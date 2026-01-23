package com.example.comunicare.domain.model

import java.util.UUID

enum class RequestStatus {
    PENDING, ASSIGNED, COMPLETED
}

enum class HelpType {
    SHOPPING, ACCOMPANIMENT, MEDICATION, EMERGENCY, OTHER, RECOVERY
}

data class HelpRequest(
    val id: String = UUID.randomUUID().toString(),
    val beneficiaryId: String,
    val beneficiaryName: String,
    val type: HelpType,
    val description: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val assignedVolunteerId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
