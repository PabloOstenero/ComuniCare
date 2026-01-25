package com.example.comunicare.domain.use_case

import com.example.comunicare.domain.repository.HelpRepository

/**
 * GetSavedSessionUseCase: Recupera el identificador único del usuario persistido.
 * RA6.d - Implementación de la recuperación de información persistente.
 */
class GetSavedSessionUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(): String? = repository.getSavedSession()
}

/**
 * SaveSessionUseCase: Registra el ID de usuario para mantener la sesión activa.
 * RA1.g - Asociación de eventos de login con persistencia.
 */
class SaveSessionUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke(userId: String) = repository.saveSession(userId)
}

/**
 * ClearSessionUseCase: Elimina los datos de sesión al cerrar la cuenta.
 * RA4.c - Acción vinculada a la opción de menú 'Cerrar sesión'.
 */
class ClearSessionUseCase(private val repository: HelpRepository) {
    suspend operator fun invoke() = repository.clearSession()
}
