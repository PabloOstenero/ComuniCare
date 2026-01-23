package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.model.RequestStatus
import com.example.comunicare.domain.repository.HelpRepository

class UpdateHelpRequestStatusUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(requestId: String, status: RequestStatus) {
        repository.updateRequestStatus(requestId, status)
    }
}
