package com.example.comunicare.domain.model

import java.util.UUID

enum class MessageType {
    TEXT, IMAGE, AUDIO
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val requestId: String,
    val senderId: String,
    val senderName: String,
    val content: String, // Texto del mensaje, URI de la imagen o URI del audio
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis()
)
