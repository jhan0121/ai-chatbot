package org.flinter.aichatbot.feedback.service

import org.flinter.aichatbot.chat.repository.ChatRepository
import org.flinter.aichatbot.chat.repository.ThreadRepository
import org.flinter.aichatbot.common.exception.AppException
import org.flinter.aichatbot.common.security.UserPrincipal
import org.flinter.aichatbot.feedback.domain.Feedback
import org.flinter.aichatbot.feedback.dto.request.CreateFeedbackRequest
import org.flinter.aichatbot.feedback.dto.request.UpdateFeedbackStatusRequest
import org.flinter.aichatbot.feedback.dto.response.FeedbackResponse
import org.flinter.aichatbot.feedback.dto.response.PagedFeedbackResponse
import org.flinter.aichatbot.feedback.repository.FeedbackRepository
import org.flinter.aichatbot.user.domain.UserRole
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val chatRepository: ChatRepository,
    private val threadRepository: ThreadRepository,
) {

    @Transactional
    fun createFeedback(principal: UserPrincipal, request: CreateFeedbackRequest): FeedbackResponse {
        val chat = chatRepository.findById(request.chatId).orElseThrow {
            AppException(HttpStatus.NOT_FOUND, "Chat not found")
        }

        if (principal.role != UserRole.ADMIN) {
            val thread = threadRepository.findById(chat.threadId).orElseThrow {
                AppException(HttpStatus.NOT_FOUND, "Thread not found")
            }
            if (thread.userId != principal.userId) {
                throw AppException(HttpStatus.FORBIDDEN, "Access denied")
            }
        }

        if (feedbackRepository.existsByUserIdAndChatId(principal.userId, request.chatId)) {
            throw AppException(HttpStatus.CONFLICT, "Feedback already exists for this chat")
        }

        val feedback = feedbackRepository.save(
            Feedback(
                userId = principal.userId,
                chatId = request.chatId,
                positive = request.positive,
            )
        )
        return FeedbackResponse.from(feedback)
    }

    @Transactional(readOnly = true)
    fun listFeedbacks(
        principal: UserPrincipal,
        positive: Boolean?,
        page: Int,
        size: Int,
        sortDirection: Sort.Direction,
    ): PagedFeedbackResponse {
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, "createdAt"))
        val feedbackPage = when {
            principal.role == UserRole.ADMIN && positive != null ->
                feedbackRepository.findAllByPositive(positive, pageable)
            principal.role == UserRole.ADMIN ->
                feedbackRepository.findAll(pageable)
            positive != null ->
                feedbackRepository.findAllByUserIdAndPositive(principal.userId, positive, pageable)
            else ->
                feedbackRepository.findAllByUserId(principal.userId, pageable)
        }
        return PagedFeedbackResponse(
            feedbacks = feedbackPage.content.map { FeedbackResponse.from(it) },
            page = feedbackPage.number,
            size = feedbackPage.size,
            totalElements = feedbackPage.totalElements,
            totalPages = feedbackPage.totalPages,
        )
    }

    @Transactional
    fun updateFeedbackStatus(feedbackId: Long, request: UpdateFeedbackStatusRequest): FeedbackResponse {
        val feedback = feedbackRepository.findById(feedbackId).orElseThrow {
            AppException(HttpStatus.NOT_FOUND, "Feedback not found")
        }
        feedback.status = request.status
        return FeedbackResponse.from(feedbackRepository.save(feedback))
    }
}
