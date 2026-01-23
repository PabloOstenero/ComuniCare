package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.model.ChatMessage
import com.example.comunicare.domain.repository.HelpRepository

class SendMessageUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(message: ChatMessage) {
        repository.sendMessage(message)
    }
}
