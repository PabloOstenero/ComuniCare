package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.model.ChatMessage
import com.example.comunicare.domain.repository.HelpRepository
import kotlinx.coroutines.flow.Flow

class GetChatMessagesUseCase(private val repository: HelpRepository) {
    operator fun invoke(requestId: String): Flow<List<ChatMessage>> = repository.getMessagesForRequest(requestId)
}
