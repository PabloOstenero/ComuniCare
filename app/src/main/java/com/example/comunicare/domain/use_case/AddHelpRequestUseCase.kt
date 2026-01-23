package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.model.HelpRequest
import com.example.comunicare.domain.repository.HelpRepository

class AddHelpRequestUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(request: HelpRequest) {
        repository.addRequest(request)
    }
}
