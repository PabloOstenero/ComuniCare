package com.example.comunicare.data.repository

import android.content.Context
import com.example.comunicare.data.local.dao.ChatMessageDao
import com.example.comunicare.data.local.dao.HelpRequestDao
import com.example.comunicare.data.local.dao.UserDao
import com.example.comunicare.data.local.entity.ChatMessageEntity
import com.example.comunicare.data.local.entity.HelpRequestEntity
import com.example.comunicare.data.local.entity.UserEntity
import com.example.comunicare.domain.model.ChatMessage
import com.example.comunicare.domain.model.HelpRequest
import com.example.comunicare.domain.model.RequestStatus
import com.example.comunicare.domain.model.User
import com.example.comunicare.domain.repository.HelpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HelpRepositoryImpl(
    private val helpRequestDao: HelpRequestDao,
    private val chatMessageDao: ChatMessageDao,
    private val userDao: UserDao,
    context: Context
) : HelpRepository {

    private val prefs = context.getSharedPreferences("comunicare_prefs", Context.MODE_PRIVATE)

    override fun getRequests(): Flow<List<HelpRequest>> {
        return helpRequestDao.getAllRequests().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addRequest(request: HelpRequest) {
        helpRequestDao.insertRequest(HelpRequestEntity.fromDomain(request))
    }

    override suspend fun updateRequestStatus(requestId: String, status: RequestStatus) {
        helpRequestDao.updateStatus(requestId, status.name)
    }

    override suspend fun assignRequest(requestId: String, status: RequestStatus, volunteerId: String) {
        helpRequestDao.assignRequest(requestId, status.name, volunteerId)
    }

    override suspend fun deleteRequest(requestId: String) {
        helpRequestDao.deleteRequest(requestId)
    }

    override fun getMessagesForRequest(requestId: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesForRequest(requestId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun sendMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(ChatMessageEntity.fromDomain(message))
    }

    override suspend fun getUserByName(name: String): User? {
        return userDao.getUserByName(name)?.toDomain()
    }

    override suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)?.toDomain()
    }

    override suspend fun saveUser(user: User) {
        userDao.insertUser(UserEntity.fromDomain(user))
    }

    override suspend fun saveSession(userId: String) {
        prefs.edit().putString("saved_user_id", userId).apply()
    }

    override suspend fun getSavedSession(): String? {
        return prefs.getString("saved_user_id", null)
    }

    override suspend fun clearSession() {
        prefs.edit().remove("saved_user_id").apply()
    }
}
