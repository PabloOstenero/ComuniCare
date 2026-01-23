package com.example.comunicare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.comunicare.domain.model.ChatMessage
import com.example.comunicare.domain.model.MessageType

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val requestId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val type: String,
    val timestamp: Long
) {
    fun toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            requestId = requestId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            type = MessageType.valueOf(type),
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(domain: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = domain.id,
                requestId = domain.requestId,
                senderId = domain.senderId,
                senderName = domain.senderName,
                content = domain.content,
                type = domain.type.name,
                timestamp = domain.timestamp
            )
        }
    }
}
