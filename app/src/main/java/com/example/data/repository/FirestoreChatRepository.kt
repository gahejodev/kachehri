package com.example.data.repository

import android.util.Log
import com.example.data.db.KachehriDao
import com.example.data.model.MessageEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository class handling real-time chat message storage and synchronization with Cloud Firestore.
 *
 * Provides bidirectional sync between local Room SQLite database and remote Cloud Firestore:
 * - Real-time snapshot streaming via Kotlin Coroutines [Flow]
 * - Message persistence (encrypted payload, IV, ephemeral metadata, view-once status)
 * - Automatic synchronizer reconciling remote incoming messages into local Room storage
 * - Status updates (DELIVERED, READ) and view-once / burn expiration synchronization
 */
class FirestoreChatRepository(
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.w(TAG, "FirebaseFirestore instance not available yet: ${e.message}")
        null
    }
) {

    companion object {
        private const val TAG = "FirestoreChatRepo"
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
    }

    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class Synced(val count: Int, val timestamp: Long = System.currentTimeMillis()) : SyncState()
        data class Error(val message: String) : SyncState()
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState = _syncState.asStateFlow()

    /**
     * Checks whether Firestore is initialized and configured with a valid, non-placeholder API key.
     */
    fun isAvailable(): Boolean {
        val db = firestore ?: return false
        return try {
            val apiKey = db.app.options.apiKey
            apiKey.isNotBlank() && !apiKey.contains("placeholder", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Stores a chat message in Firestore under `chats/{chatId}/messages/{messageId}`.
     * Also updates the parent conversation metadata with the latest message snippet and timestamp.
     */
    suspend fun saveMessage(chatId: String, message: MessageEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            val messageRef = db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .document(message.id)

            val data = message.toFirestoreMap()
            messageRef.set(data, SetOptions.merge()).awaitResult()

            // Update parent chat document with latest preview
            val previewText = when {
                message.isViewOnce -> "① ${if (message.mediaType == "PHOTO") "Photo" else "Media"}"
                message.mediaType == "VOICE" -> "▶ Voice note (${message.voiceDurationSeconds}s)"
                message.mediaType == "PHOTO" -> "📷 Photo"
                else -> message.text
            }

            val chatMeta = mapOf(
                "lastMessageText" to previewText,
                "lastMessageTimestamp" to message.timestamp,
                "lastSenderId" to message.senderId,
                "disappearingSeconds" to message.disappearingSeconds
            )
            db.collection(CHATS_COLLECTION).document(chatId)
                .set(chatMeta, SetOptions.merge()).awaitResult()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error saving message ${message.id} to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Listens in real-time to messages in a specific chat, emitting updated message lists whenever
     * Firestore changes (new messages, status changes, burn events).
     */
    fun observeRealtimeMessages(chatId: String): Flow<List<MessageEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            Log.w(TAG, "Firestore not initialized; cannot observe real-time messages for chat: $chatId")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val messagesRef = db.collection(CHATS_COLLECTION)
            .document(chatId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val registration = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Firestore snapshot listener error for chat $chatId: ${error.message}")
                _syncState.value = SyncState.Error(error.message ?: "Sync error")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { doc ->
                    doc.toMessageEntity()
                }
                _syncState.value = SyncState.Synced(messages.size)
                trySend(messages)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Synchronizes real-time Firestore messages into the local Room SQLite database for a chat.
     * When remote peers send messages, this immediately writes them to local storage, updating UI flows.
     */
    fun startRealtimeSync(
        chatId: String,
        dao: KachehriDao,
        scope: CoroutineScope
    ) = scope.launch(Dispatchers.IO) {
        if (!isAvailable()) {
            Log.d(TAG, "Firestore offline or not configured; skipping remote sync loop for $chatId")
            return@launch
        }

        _syncState.value = SyncState.Syncing

        try {
            // First push any pending local messages to Firestore
            syncPendingLocalMessages(chatId, dao)

            // Then observe real-time remote stream and persist into Room
            observeRealtimeMessages(chatId).collect { remoteMessages ->
                for (remoteMsg in remoteMessages) {
                    dao.insertMessage(remoteMsg)
                }
                _syncState.value = SyncState.Synced(remoteMessages.size)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in real-time sync for chat $chatId", e)
            _syncState.value = SyncState.Error(e.message ?: "Sync failed")
        }
    }

    /**
     * Uploads any local messages in Room that might not yet be stored in Firestore.
     */
    suspend fun syncPendingLocalMessages(chatId: String, dao: KachehriDao) {
        val db = firestore ?: return
        try {
            val localMessages = dao.getMessagesForChat(chatId).first()
            for (localMsg in localMessages) {
                val docRef = db.collection(CHATS_COLLECTION)
                    .document(chatId)
                    .collection(MESSAGES_COLLECTION)
                    .document(localMsg.id)

                val doc = docRef.get().awaitResult()
                if (!doc.exists()) {
                    docRef.set(localMsg.toFirestoreMap(), SetOptions.merge()).awaitResult()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing pending local messages: ${e.message}")
        }
    }

    /**
     * Updates the delivery/read status of a message in Firestore.
     */
    suspend fun updateMessageStatus(chatId: String, messageId: String, status: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update("status", status)
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marks a view-once message as opened in Firestore so all devices reflect the consumed state.
     */
    suspend fun markViewOnceOpened(chatId: String, messageId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update(
                    mapOf(
                        "isOpened" to true,
                        "cipherTextHex" to "", // Purge encrypted payload from cloud upon view
                        "text" to "View once photo (opened)"
                    )
                )
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marks an expired disappearing message as burned in Firestore.
     */
    suspend fun markMessageBurned(chatId: String, messageId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update(
                    mapOf(
                        "isBurned" to true,
                        "cipherTextHex" to "",
                        "text" to "This message has disappeared"
                    )
                )
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a specific message from Firestore.
     */
    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .document(messageId)
                .delete()
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clears all messages for a chat in Firestore.
     */
    suspend fun clearChatMessages(chatId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            val messages = db.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .get()
                .awaitResult()

            val batch = db.batch()
            for (doc in messages.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().awaitResult()

            db.collection(CHATS_COLLECTION).document(chatId)
                .update("lastMessageText", "Chat cleared", "lastMessageTimestamp", System.currentTimeMillis())
                .awaitResult()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// -----------------------------------------------------------------------------------------
// Helper Serialization Extensions
// -----------------------------------------------------------------------------------------

private fun MessageEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "chatId" to chatId,
    "senderId" to senderId,
    "text" to text,
    "cipherTextHex" to cipherTextHex,
    "ivHex" to ivHex,
    "timestamp" to timestamp,
    "isOutgoing" to isOutgoing,
    "status" to status,
    "isViewOnce" to isViewOnce,
    "isOpened" to isOpened,
    "mediaType" to mediaType,
    "mediaResName" to mediaResName,
    "voiceDurationSeconds" to voiceDurationSeconds,
    "disappearingSeconds" to disappearingSeconds,
    "expiresAt" to expiresAt,
    "isBurned" to isBurned
)

private fun DocumentSnapshot.toMessageEntity(): MessageEntity? {
    val id = getString("id") ?: id
    val chatId = getString("chatId") ?: return null
    val senderId = getString("senderId") ?: ""
    val text = getString("text") ?: ""
    val cipherTextHex = getString("cipherTextHex") ?: ""
    val ivHex = getString("ivHex") ?: ""
    val timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    val isOutgoing = getBoolean("isOutgoing") ?: (senderId == "me")
    val status = getString("status") ?: "DELIVERED"
    val isViewOnce = getBoolean("isViewOnce") ?: false
    val isOpened = getBoolean("isOpened") ?: false
    val mediaType = getString("mediaType") ?: "NONE"
    val mediaResName = getString("mediaResName")
    val voiceDurationSeconds = getLong("voiceDurationSeconds")?.toInt() ?: 0
    val disappearingSeconds = getLong("disappearingSeconds")?.toInt() ?: 0
    val expiresAt = getLong("expiresAt")
    val isBurned = getBoolean("isBurned") ?: false

    return MessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        cipherTextHex = cipherTextHex,
        ivHex = ivHex,
        timestamp = timestamp,
        isOutgoing = isOutgoing,
        status = status,
        isViewOnce = isViewOnce,
        isOpened = isOpened,
        mediaType = mediaType,
        mediaResName = mediaResName,
        voiceDurationSeconds = voiceDurationSeconds,
        disappearingSeconds = disappearingSeconds,
        expiresAt = expiresAt,
        isBurned = isBurned
    )
}

/**
 * Await helper converting Task<T> to a coroutine with cancellation support.
 */
private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) {
            continuation.resumeWithException(exception)
        }
    }
}
