package com.example.data.repository

import com.example.data.crypto.E2EEEngine
import com.example.data.db.KachehriDao
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatEntity
import com.example.data.model.ChatWithContact
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusUpdateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class KachehriRepository(
    private val dao: KachehriDao,
    val firestoreRepo: FirestoreChatRepository = FirestoreChatRepository()
) {

    fun getAllChats(): Flow<List<ChatWithContact>> = dao.getAllChatsWithContact()

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> = dao.getMessagesForChat(chatId)

    fun getContact(contactId: String): Flow<ContactEntity?> = dao.getContactById(contactId)

    fun getAllContacts(): Flow<List<ContactEntity>> = dao.getAllContacts()

    fun getActiveStatusUpdates(): Flow<List<StatusUpdateEntity>> =
        dao.getActiveStatusUpdates(System.currentTimeMillis())

    fun getAllCalls(): Flow<List<CallLogEntity>> = dao.getAllCallLogs()

    suspend fun sendMessage(
        chatId: String,
        contactId: String,
        text: String,
        isViewOnce: Boolean = false,
        mediaType: String = "NONE",
        mediaResName: String? = null,
        voiceDurationSeconds: Int = 0,
        disappearingSeconds: Int = 0
    ) {
        val encrypted = E2EEEngine.encrypt(text, chatId)
        val now = System.currentTimeMillis()
        val expiresAt = if (disappearingSeconds > 0) now + (disappearingSeconds * 1000L) else null

        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "me",
            text = text,
            cipherTextHex = encrypted.cipherTextHex,
            ivHex = encrypted.ivHex,
            timestamp = now,
            isOutgoing = true,
            status = "PENDING",
            isViewOnce = isViewOnce,
            isOpened = false,
            mediaType = mediaType,
            mediaResName = mediaResName,
            voiceDurationSeconds = voiceDurationSeconds,
            disappearingSeconds = disappearingSeconds,
            expiresAt = expiresAt
        )

        dao.insertMessage(message)

        val preview = when {
            isViewOnce -> "① ${if (mediaType == "PHOTO") "Photo" else "Media"}"
            mediaType == "VOICE" -> "▶ Voice note (${voiceDurationSeconds}s)"
            mediaType == "PHOTO" -> "📷 Photo"
            else -> text
        }
        dao.updateChatLastMessage(chatId, preview, now)

        // Asynchronously synchronize to Firestore
        if (firestoreRepo.isAvailable()) {
            CoroutineScope(Dispatchers.IO).launch {
                firestoreRepo.saveMessage(chatId, message)
            }
        }
    }

    suspend fun markViewOnceOpened(messageId: String, chatId: String? = null) {
        dao.markViewOnceOpened(messageId)
        if (chatId != null && firestoreRepo.isAvailable()) {
            CoroutineScope(Dispatchers.IO).launch {
                firestoreRepo.markViewOnceOpened(chatId, messageId)
            }
        }
    }

    fun startRealtimeSync(chatId: String, scope: CoroutineScope): Job {
        return firestoreRepo.startRealtimeSync(chatId, dao, scope)
    }

    suspend fun burnExpiredMessages() {
        dao.burnExpiredMessages(System.currentTimeMillis())
    }

    suspend fun setChatDisappearingTimer(contactId: String, seconds: Int) {
        dao.setChatDisappearingTimer(contactId, seconds)
    }

    suspend fun setContactVerified(contactId: String, verified: Boolean) {
        dao.setContactVerified(contactId, verified)
    }

    suspend fun markChatAsRead(chatId: String) {
        dao.markChatAsRead(chatId)
    }

    suspend fun clearChat(chatId: String) {
        dao.clearChatMessages(chatId)
        dao.updateChatLastMessage(chatId, "Chat cleared", System.currentTimeMillis())
        if (firestoreRepo.isAvailable()) {
            CoroutineScope(Dispatchers.IO).launch {
                firestoreRepo.clearChatMessages(chatId)
            }
        }
    }

    suspend fun createNewChat(contact: ContactEntity): String {
        dao.insertContact(contact)
        val chatId = "chat_${contact.id}"
        val chat = ChatEntity(
            id = chatId,
            contactId = contact.id,
            unreadCount = 0,
            isPinned = false,
            lastMessageText = "Chat started",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        dao.insertChat(chat)
        return chatId
    }

    suspend fun postStatus(text: String, imageResName: String? = null, bgColor: Long = 0xFF00796B) {
        val status = StatusUpdateEntity(
            id = UUID.randomUUID().toString(),
            contactId = "me",
            userName = "My Status",
            avatarColor = 0xFF00A884,
            timestamp = System.currentTimeMillis(),
            text = text,
            imageResName = imageResName,
            backgroundColor = bgColor,
            isViewed = false,
            expiresAt = System.currentTimeMillis() + 86400000L
        )
        dao.insertStatusUpdate(status)
    }

    suspend fun logCall(contactId: String, contactName: String, contactAvatarColor: Long, isVideo: Boolean, isOutgoing: Boolean, duration: Int) {
        val call = CallLogEntity(
            id = UUID.randomUUID().toString(),
            contactId = contactId,
            contactName = contactName,
            contactAvatarColor = contactAvatarColor,
            timestamp = System.currentTimeMillis(),
            isVideo = isVideo,
            isOutgoing = isOutgoing,
            isMissed = duration == 0 && !isOutgoing,
            durationSeconds = duration,
            e2eeVerified = false
        )
        dao.insertCallLog(call)
    }
}
