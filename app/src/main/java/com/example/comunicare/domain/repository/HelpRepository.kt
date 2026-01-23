package com.example.comunicare.domain.repository

import com.example.comunicare.domain.model.ChatMessage
import com.example.comunicare.domain.model.HelpRequest
import com.example.comunicare.domain.model.User
import kotlinx.coroutines.flow.Flow

interface HelpRepository {
    fun getRequests(): Flow<List<HelpRequest>>
    suspend fun addRequest(request: HelpRequest)
    suspend fun updateRequestStatus(requestId: String, status: com.example.comunicare.domain.model.RequestStatus)
    suspend fun deleteRequest(requestId: String)
    
    // Chat functionality
    fun getMessagesForRequest(requestId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: ChatMessage)

    // User management
    suspend fun getUserByName(name: String): User?
    suspend fun saveUser(user: User)
}
