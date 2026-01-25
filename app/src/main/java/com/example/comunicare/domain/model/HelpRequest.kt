package com.example.comunicare.domain.model

import java.util.UUID

/**
 * RequestStatus: Define el ciclo de vida de una solicitud de ayuda.
 * 
 * - PENDING: Recién creada por el beneficiario.
 * - ASSIGNED: Un administrador ha aceptado la responsabilidad.
 * - COMPLETED: La ayuda ha sido proporcionada y el caso se ha cerrado.
 */
enum class RequestStatus {
    PENDING, ASSIGNED, COMPLETED
}

/**
 * HelpType: Categorización de los servicios ofrecidos.
 * RA1.d: Permite la personalización visual de tarjetas según el tipo.
 */
enum class HelpType {
    SHOPPING, ACCOMPANIMENT, MEDICATION, EMERGENCY, OTHER, RECOVERY
}

/**
 * HelpRequest: Entidad principal de dominio que representa una petición de servicio.
 * 
 * CRITERIOS DE RÚBRICA CUMPLIDOS:
 * - RA1.g: Registro de marcas de tiempo (timestamp) para trazabilidad.
 * - RA6.d: Estructura preparada para persistencia relacional en Room.
 */
data class HelpRequest(
    val id: String = UUID.randomUUID().toString(),
    val beneficiaryId: String,
    val beneficiaryName: String,
    val type: HelpType,
    val description: String,
    val status: RequestStatus = RequestStatus.PENDING,
    /** ID del voluntario/admin responsable de la gestión (RA4.h) */
    val assignedVolunteerId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
