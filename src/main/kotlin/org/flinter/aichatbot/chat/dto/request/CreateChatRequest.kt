package org.flinter.aichatbot.chat.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateChatRequest(
    @field:NotBlank val question: String,
    val isStreaming: Boolean = false,
    @field:NotBlank val model: String = "gpt-5.4-mini",
)
