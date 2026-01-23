package com.example.comunicare.data.local.dao

import androidx.room.*
import com.example.comunicare.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE requestId = :requestId ORDER BY timestamp ASC")
    fun getMessagesForRequest(requestId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}
