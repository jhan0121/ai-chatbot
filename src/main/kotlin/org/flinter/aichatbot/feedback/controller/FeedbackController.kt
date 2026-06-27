package org.flinter.aichatbot.feedback.controller

import jakarta.validation.Valid
import org.flinter.aichatbot.common.dto.ApiResponse
import org.flinter.aichatbot.common.security.UserPrincipal
import org.flinter.aichatbot.common.security.annotation.Admin
import org.flinter.aichatbot.common.security.annotation.Auth
import org.flinter.aichatbot.feedback.dto.request.CreateFeedbackRequest
import org.flinter.aichatbot.feedback.dto.request.UpdateFeedbackStatusRequest
import org.flinter.aichatbot.feedback.dto.response.FeedbackResponse
import org.flinter.aichatbot.feedback.dto.response.PagedFeedbackResponse
import org.flinter.aichatbot.feedback.service.FeedbackService
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@RestController
@RequestMapping("/api/feedbacks")
class FeedbackController(private val feedbackService: FeedbackService) {

    @Auth
    @PostMapping
    fun createFeedback(
        @Valid @RequestBody request: CreateFeedbackRequest,
    ): ResponseEntity<ApiResponse<FeedbackResponse>> {
        val feedback = feedbackService.createFeedback(currentPrincipal(), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(feedback))
    }

    @Auth
    @GetMapping
    fun listFeedbacks(
        @RequestParam(required = false) positive: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
    ): ResponseEntity<ApiResponse<PagedFeedbackResponse>> {
        val direction = if (sort.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        return ResponseEntity.ok(
            ApiResponse.success(feedbackService.listFeedbacks(currentPrincipal(), positive, page, size, direction))
        )
    }

    @Admin
    @PatchMapping("/{feedbackId}/status")
    fun updateFeedbackStatus(
        @PathVariable feedbackId: Long,
        @Valid @RequestBody request: UpdateFeedbackStatusRequest,
    ): ResponseEntity<ApiResponse<FeedbackResponse>> {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.updateFeedbackStatus(feedbackId, request)))
    }

    private fun currentPrincipal(): UserPrincipal =
        (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes)
            .request.getAttribute("principal") as UserPrincipal
}
