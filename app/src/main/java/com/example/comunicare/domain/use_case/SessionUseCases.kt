package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.repository.HelpRepository

class GetSavedSessionUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(): String? = repository.getSavedSession()
}

class SaveSessionUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(userId: String) = repository.saveSession(userId)
}

class ClearSessionUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke() = repository.clearSession()
}
