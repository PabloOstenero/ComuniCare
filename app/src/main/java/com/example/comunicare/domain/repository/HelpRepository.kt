package com.example.comunicare.domain.repository

import com.example.comunicare.domain.model.ChatMessage
import com.example.comunicare.domain.model.HelpRequest
import com.example.comunicare.domain.model.RequestStatus
import com.example.comunicare.domain.model.User
import kotlinx.coroutines.flow.Flow

interface HelpRepository {
    fun getRequests(): Flow<List<HelpRequest>>
    suspend fun addRequest(request: HelpRequest)
    suspend fun updateRequestStatus(requestId: String, status: RequestStatus)
    suspend fun assignRequest(requestId: String, status: RequestStatus, volunteerId: String)
    suspend fun deleteRequest(requestId: String)
    
    // Chat functionality
    fun getMessagesForRequest(requestId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: ChatMessage)

    // User management
    suspend fun getUserByName(name: String): User?
    suspend fun getUserByPhoneNumber(phoneNumber: String): User?
    suspend fun getUserById(id: String): User?
    suspend fun saveUser(user: User)
    
    // Session management
    suspend fun saveSession(userId: String)
    suspend fun getSavedSession(): String?
    suspend fun clearSession()
}
