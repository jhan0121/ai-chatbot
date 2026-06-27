package org.flinter.aichatbot.feedback.dto.request

import jakarta.validation.constraints.NotNull

data class CreateFeedbackRequest(
    @field:NotNull val chatId: Long,
    @field:NotNull val positive: Boolean,
)
