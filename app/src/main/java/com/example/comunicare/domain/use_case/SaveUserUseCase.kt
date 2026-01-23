package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.model.User
import com.example.comunicare.domain.repository.HelpRepository

class SaveUserUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(user: User) = repository.saveUser(user)
}
