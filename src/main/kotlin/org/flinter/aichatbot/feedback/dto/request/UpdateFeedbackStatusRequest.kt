package org.flinter.aichatbot.feedback.dto.request

import jakarta.validation.constraints.NotNull
import org.flinter.aichatbot.feedback.domain.FeedbackStatus

data class UpdateFeedbackStatusRequest(
    @field:NotNull val status: FeedbackStatus,
)
