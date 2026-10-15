package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithContact(
    @Embedded val chat: ChatEntity,
    @Relation(
        parentColumn = "contactId",
        entityColumn = "id"
    )
    val contact: ContactEntity?
)
