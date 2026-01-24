package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.model.User
import com.example.comunicare.domain.repository.HelpRepository

class GetUserByIdUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(id: String): User? = repository.getUserById(id)
}
