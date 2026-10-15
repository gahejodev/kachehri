package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "status_updates")
data class StatusUpdateEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val userName: String,
    val avatarColor: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val text: String? = null,
    val imageResName: String? = null,
    val backgroundColor: Long = 0xFF075E54,
    val isViewed: Boolean = false,
    val expiresAt: Long = System.currentTimeMillis() + 86400000L // 24 hours
)
