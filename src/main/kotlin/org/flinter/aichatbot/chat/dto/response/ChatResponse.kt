package org.flinter.aichatbot.chat.dto.response

import org.flinter.aichatbot.chat.domain.Chat
import java.time.OffsetDateTime

data class ChatResponse(
    val id: Long,
    val threadId: Long,
    val question: String,
    val answer: String,
    val model: String,
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(chat: Chat) = ChatResponse(
            id = chat.id,
            threadId = chat.threadId,
            question = chat.question,
            answer = chat.answer,
            model = chat.model,
            createdAt = chat.createdAt,
        )
    }
}
