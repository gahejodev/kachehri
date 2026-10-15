package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val avatarColor: Long,
    val avatarResName: String? = null,
    val statusText: String,
    val safetyNumber: String,
    val isVerified: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeen: String = "online",
    val disappearingTimerSeconds: Int = 0 // 0 = Off, 10 = 10s test mode, 86400 = 24h, 604800 = 7d
)
