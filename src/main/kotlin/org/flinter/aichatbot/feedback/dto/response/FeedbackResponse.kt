package org.flinter.aichatbot.feedback.dto.response

import org.flinter.aichatbot.feedback.domain.Feedback
import org.flinter.aichatbot.feedback.domain.FeedbackStatus
import java.time.OffsetDateTime

data class FeedbackResponse(
    val id: Long,
    val chatId: Long,
    val positive: Boolean,
    val status: FeedbackStatus,
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(feedback: Feedback) = FeedbackResponse(
            id = feedback.id,
            chatId = feedback.chatId,
            positive = feedback.positive,
            status = feedback.status,
            createdAt = feedback.createdAt,
        )
    }
}
