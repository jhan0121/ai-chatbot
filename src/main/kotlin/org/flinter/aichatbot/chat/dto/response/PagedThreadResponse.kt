package org.flinter.aichatbot.chat.dto.response

data class PagedThreadResponse(
    val threads: List<ThreadWithChatsResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
