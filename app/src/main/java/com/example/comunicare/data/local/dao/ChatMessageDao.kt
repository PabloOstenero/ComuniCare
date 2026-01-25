package com.example.comunicare.data.local.dao

import androidx.room.*
import com.example.comunicare.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * ChatMessageDao: Interfaz de acceso a datos para la mensajería.
 * RA6.d - Definición de consultas para persistencia local reactiva.
 */
@Dao
interface ChatMessageDao {
    /**
     * Recupera el historial de mensajes de una solicitud específica.
     * Utiliza Flow para actualizaciones automáticas en la UI (RA1.g).
     */
    @Query("SELECT * FROM chat_messages WHERE requestId = :requestId ORDER BY timestamp ASC")
    fun getMessagesForRequest(requestId: String): Flow<List<ChatMessageEntity>>

    /** Inserta un nuevo mensaje multimedia en el historial */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}
