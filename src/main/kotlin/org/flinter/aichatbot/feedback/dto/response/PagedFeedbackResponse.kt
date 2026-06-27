package org.flinter.aichatbot.feedback.dto.response

data class PagedFeedbackResponse(
    val feedbacks: List<FeedbackResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
