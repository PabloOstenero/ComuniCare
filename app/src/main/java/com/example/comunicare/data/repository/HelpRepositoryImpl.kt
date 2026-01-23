package com.example.comunicare.data.repository

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
    private val userDao: UserDao
) : HelpRepository {

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

    override suspend fun saveUser(user: User) {
        userDao.insertUser(UserEntity.fromDomain(user))
    }
}
