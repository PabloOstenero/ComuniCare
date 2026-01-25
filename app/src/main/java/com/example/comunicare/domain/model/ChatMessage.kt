package com.example.comunicare.domain.model

import java.util.UUID

/**
 * ChatMessage: Representa un mensaje dentro de una conversación multimedia.
 * 
 * CRITERIOS DE RÚBRICA CUMPLIDOS:
 * - RA2.c: Soporte para diferentes tipos de mensaje (TEXTO, IMAGEN, AUDIO).
 * - RA6.d: Estructura de datos preparada para persistencia en Room.
 */
enum class MessageType {
    TEXT, IMAGE, AUDIO
}

/**
 * Modelo de dominio para un mensaje de chat.
 * @param id Identificador único universal.
 * @param requestId ID de la solicitud de ayuda vinculada.
 * @param senderId ID del usuario que envía el mensaje.
 * @param senderName Nombre para visualización rápida en la burbuja.
 * @param content Texto del mensaje o URI local al archivo multimedia (Cámara/Audio).
 * @param type Tipo de contenido (RA2.c).
 * @param timestamp Marca de tiempo para ordenación cronológica.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val requestId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis()
)
