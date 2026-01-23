package com.example.comunicare.data.local.dao

import androidx.room.*
import com.example.comunicare.data.local.entity.HelpRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HelpRequestDao {
    @Query("SELECT * FROM help_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<HelpRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: HelpRequestEntity)

    @Query("UPDATE help_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM help_requests WHERE id = :id")
    suspend fun deleteRequest(id: String)
}
