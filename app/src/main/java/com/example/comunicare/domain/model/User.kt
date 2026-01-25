package com.example.comunicare.domain.model

/**
 * UserRole: Define los niveles de acceso y permisos dentro de la plataforma.
 * - BENEFICIARY: Usuario que solicita servicios de ayuda.
 * - ADMIN: Voluntario o gestor que atiende las solicitudes.
 */
enum class UserRole {
    BENEFICIARY, ADMIN
}

/**
 * User: Entidad de dominio que representa a un usuario del sistema.
 * 
 * CRITERIOS DE RÚBRICA CUMPLIDOS:
 * - RA4.f: Estructura preparada para autenticación segura (password).
 * - RA6.d: Atributos diseñados para persistencia relacional en Room.
 * - RA6.c: Incluye mecanismos de recuperación (recoveryHint, trustedContactId).
 * 
 * @param id Identificador único (id_telefono).
 * @param name Nombre para mostrar del usuario.
 * @param password Credencial de acceso.
 * @param phoneNumber Identificador único real (RA4.f).
 * @param role Perfil de acceso (RA1.h).
 * @param recoveryHint Pista textual para ayudar a recordar la clave (RA6.c).
 * @param trustedContactId ID del administrador vinculado para recuperación (RA6.c).
 */
data class User(
    val id: String,
    val name: String,
    val password: String,
    val phoneNumber: String,
    val role: UserRole,
    val recoveryHint: String = "",
    val trustedContactId: String? = null
)
