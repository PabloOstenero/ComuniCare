package com.example.comunicare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.comunicare.domain.repository.HelpRepository
import com.example.comunicare.domain.use_case.*

class HelpViewModelFactory(private val repository: HelpRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HelpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HelpViewModel(
                getHelpRequestsUseCase = GetHelpRequestsUseCase(repository),
                addHelpRequestUseCase = AddHelpRequestUseCase(repository),
                updateHelpRequestStatusUseCase = UpdateHelpRequestStatusUseCase(repository),
                assignHelpRequestUseCase = AssignHelpRequestUseCase(repository),
                getChatMessagesUseCase = GetChatMessagesUseCase(repository),
                sendMessageUseCase = SendMessageUseCase(repository),
                getUserUseCase = GetUserUseCase(repository),
                getUserByPhoneNumberUseCase = GetUserByPhoneNumberUseCase(repository),
                saveUserUseCase = SaveUserUseCase(repository),
                getSavedSessionUseCase = GetSavedSessionUseCase(repository),
                saveSessionUseCase = SaveSessionUseCase(repository),
                clearSessionUseCase = ClearSessionUseCase(repository),
                getUserByIdUseCase = GetUserByIdUseCase(repository)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
