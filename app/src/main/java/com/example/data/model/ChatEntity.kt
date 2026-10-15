package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val encryptionCipher: String = "AES-256-GCM / Ratchet v3"
)
