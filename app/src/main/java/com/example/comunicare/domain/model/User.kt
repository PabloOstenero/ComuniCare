package com.example.comunicare.domain.model

enum class UserRole {
    BENEFICIARY, ADMIN
}

data class User(
    val id: String,
    val name: String,
    val password: String,
    val role: UserRole,
    val recoveryHint: String = "",
    val trustedContactId: String? = null
)
