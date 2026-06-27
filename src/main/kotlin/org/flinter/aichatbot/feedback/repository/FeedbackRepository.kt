package org.flinter.aichatbot.feedback.repository

import org.flinter.aichatbot.feedback.domain.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FeedbackRepository : JpaRepository<Feedback, Long> {

    fun existsByUserIdAndChatId(userId: Long, chatId: Long): Boolean

    fun findAllByUserId(userId: Long, pageable: Pageable): Page<Feedback>

    fun findAllByUserIdAndPositive(userId: Long, positive: Boolean, pageable: Pageable): Page<Feedback>

    fun findAllByPositive(positive: Boolean, pageable: Pageable): Page<Feedback>
}
