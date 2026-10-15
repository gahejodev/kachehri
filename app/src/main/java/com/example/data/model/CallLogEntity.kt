package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val contactName: String,
    val contactAvatarColor: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val isVideo: Boolean = false,
    val isOutgoing: Boolean = false,
    val isMissed: Boolean = false,
    val durationSeconds: Int = 0,
    val e2eeVerified: Boolean = true
)
