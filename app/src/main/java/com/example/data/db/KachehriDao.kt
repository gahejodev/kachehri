package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatEntity
import com.example.data.model.ChatWithContact
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusUpdateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KachehriDao {

    @Transaction
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllChatsWithContact(): Flow<List<ChatWithContact>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isBurned = 0 ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM contacts WHERE id = :contactId LIMIT 1")
    fun getContactById(contactId: String): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM status_updates WHERE expiresAt > :currentTime ORDER BY timestamp DESC")
    fun getActiveStatusUpdates(currentTime: Long): Flow<List<StatusUpdateEntity>>

    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET isOpened = 1 WHERE id = :messageId")
    suspend fun markViewOnceOpened(messageId: String)

    @Query("UPDATE messages SET isBurned = 1 WHERE expiresAt IS NOT NULL AND expiresAt <= :currentTime")
    suspend fun burnExpiredMessages(currentTime: Long)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearChatMessages(chatId: String)

    @Query("UPDATE contacts SET disappearingTimerSeconds = :seconds WHERE id = :contactId")
    suspend fun setChatDisappearingTimer(contactId: String, seconds: Int)

    @Query("UPDATE contacts SET isVerified = :isVerified WHERE id = :contactId")
    suspend fun setContactVerified(contactId: String, isVerified: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("UPDATE chats SET lastMessageText = :text, lastMessageTimestamp = :timestamp WHERE id = :chatId")
    suspend fun updateChatLastMessage(chatId: String, text: String, timestamp: Long)

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun markChatAsRead(chatId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusUpdate(status: StatusUpdateEntity)

    @Query("UPDATE status_updates SET isViewed = 1 WHERE id = :id")
    suspend fun markStatusViewed(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(call: CallLogEntity)
}
