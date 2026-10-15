package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatEntity
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusUpdateEntity

@Database(
    entities = [
        ContactEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        StatusUpdateEntity::class,
        CallLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KachehriDatabase : RoomDatabase() {

    abstract fun kachehriDao(): KachehriDao

    companion object {
        @Volatile
        private var INSTANCE: KachehriDatabase? = null

        fun getDatabase(context: Context): KachehriDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KachehriDatabase::class.java,
                    "kachehri_encrypted_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
