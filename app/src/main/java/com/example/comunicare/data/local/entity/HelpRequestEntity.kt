package com.example.comunicare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.comunicare.domain.model.HelpRequest
import com.example.comunicare.domain.model.HelpType
import com.example.comunicare.domain.model.RequestStatus

@Entity(tableName = "help_requests")
data class HelpRequestEntity(
    @PrimaryKey val id: String,
    val beneficiaryId: String,
    val beneficiaryName: String,
    val type: String,
    val description: String,
    val status: String,
    val assignedVolunteerId: String?,
    val timestamp: Long
) {
    fun toDomain(): HelpRequest {
        return HelpRequest(
            id = id,
            beneficiaryId = beneficiaryId,
            beneficiaryName = beneficiaryName,
            type = HelpType.valueOf(type),
            description = description,
            status = RequestStatus.valueOf(status),
            assignedVolunteerId = assignedVolunteerId,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(domain: HelpRequest): HelpRequestEntity {
            return HelpRequestEntity(
                id = domain.id,
                beneficiaryId = domain.beneficiaryId,
                beneficiaryName = domain.beneficiaryName,
                type = domain.type.name,
                description = domain.description,
                status = domain.status.name,
                assignedVolunteerId = domain.assignedVolunteerId,
                timestamp = domain.timestamp
            )
        }
    }
}
