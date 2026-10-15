package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String, // "me" or contactId
    val text: String,
    val cipherTextHex: String,
    val ivHex: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean,
    val status: String = "READ", // "SENDING", "SENT", "DELIVERED", "READ"
    val isViewOnce: Boolean = false,
    val isOpened: Boolean = false,
    val mediaType: String = "NONE", // "NONE", "PHOTO", "VOICE", "DOC"
    val mediaResName: String? = null,
    val voiceDurationSeconds: Int = 0,
    val disappearingSeconds: Int = 0, // 0 = permanent
    val expiresAt: Long? = null, // timestamp when to burn
    val isBurned: Boolean = false
)
