package com.example.comunicare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.comunicare.domain.repository.HelpRepository
import com.example.comunicare.domain.use_case.*

class HelpViewModelFactory(private val repository: HelpRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HelpViewModel::class.java)) {
            val getUseCase = GetHelpRequestsUseCase(repository)
            val addUseCase = AddHelpRequestUseCase(repository)
            val updateUseCase = UpdateHelpRequestStatusUseCase(repository)
            val getChatMessagesUseCase = GetChatMessagesUseCase(repository)
            val sendMessageUseCase = SendMessageUseCase(repository)
            @Suppress("UNCHECKED_CAST")
            return HelpViewModel(
                getUseCase,
                addUseCase,
                updateUseCase,
                getChatMessagesUseCase,
                sendMessageUseCase,
                getUserUseCase = GetUserUseCase(repository),
                saveUserUseCase = SaveUserUseCase(repository)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
