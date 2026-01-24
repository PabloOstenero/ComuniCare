package com.example.comunicare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.comunicare.domain.model.User
import com.example.comunicare.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val password: String,
    val phoneNumber: String,
    val role: String,
    val recoveryHint: String,
    val trustedContactId: String?
) {
    fun toDomain(): User {
        return User(
            id = id,
            name = name,
            password = password,
            phoneNumber = phoneNumber,
            role = UserRole.valueOf(role),
            recoveryHint = recoveryHint,
            trustedContactId = trustedContactId
        )
    }

    companion object {
        fun fromDomain(domain: User): UserEntity {
            return UserEntity(
                id = domain.id,
                name = domain.name,
                password = domain.password,
                phoneNumber = domain.phoneNumber,
                role = domain.role.name,
                recoveryHint = domain.recoveryHint,
                trustedContactId = domain.trustedContactId
            )
        }
    }
}
