package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.model.RequestStatus
import com.example.comunicare.domain.repository.HelpRepository

class AssignHelpRequestUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(requestId: String, status: RequestStatus, volunteerId: String) {
        repository.assignRequest(requestId, status, volunteerId)
    }
}
