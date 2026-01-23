package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.model.HelpRequest
import com.example.comunicare.domain.repository.HelpRepository
import kotlinx.coroutines.flow.Flow

class GetHelpRequestsUseCase(private val repository: HelpRepository) {
    operator fun invoke(): Flow<List<HelpRequest>> = repository.getRequests()
}
