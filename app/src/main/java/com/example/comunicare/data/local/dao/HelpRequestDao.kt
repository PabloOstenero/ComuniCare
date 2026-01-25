package com.example.comunicare.data.local.dao

import androidx.room.*
import com.example.comunicare.data.local.entity.HelpRequestEntity
import kotlinx.coroutines.flow.Flow

/**
 * HelpRequestDao: Interfaz de acceso a datos para las solicitudes de servicio.
 * RA6.d - Definición de operaciones de persistencia local.
 */
@Dao
interface HelpRequestDao {
    /**
     * Recupera el historial completo de avisos ordenados por fecha.
     * Implementa flujos reactivos (Flow) para la actualización en tiempo real (RA1.g).
     */
    @Query("SELECT * FROM help_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<HelpRequestEntity>>

    /** Inserta una nueva solicitud generada por el beneficiario */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: HelpRequestEntity)

    /** Actualiza únicamente el estado de resolución de un aviso */
    @Query("UPDATE help_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    /** Asigna un voluntario responsable y cambia el estado a 'En proceso' (RA4.h) */
    @Query("UPDATE help_requests SET status = :status, assignedVolunteerId = :volunteerId WHERE id = :id")
    suspend fun assignRequest(id: String, status: String, volunteerId: String)

    /** Elimina un aviso del registro histórico */
    @Query("DELETE FROM help_requests WHERE id = :id")
    suspend fun deleteRequest(id: String)
}
