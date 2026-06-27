package org.flinter.aichatbot.chat.controller

import jakarta.validation.Valid
import org.flinter.aichatbot.chat.dto.request.CreateChatRequest
import org.flinter.aichatbot.chat.dto.response.PagedThreadResponse
import org.flinter.aichatbot.chat.service.ChatService
import org.flinter.aichatbot.common.dto.ApiResponse
import org.flinter.aichatbot.common.security.UserPrincipal
import org.flinter.aichatbot.common.security.annotation.Auth
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@RestController
@RequestMapping("/api")
class ChatController(private val chatService: ChatService) {

    @Auth
    @PostMapping("/chats")
    fun createChat(
        @Valid @RequestBody request: CreateChatRequest,
    ): Any {
        val principal = currentPrincipal()
        return if (request.isStreaming) {
            chatService.createChatStream(principal, request)
        } else {
            val chat = chatService.createChat(principal, request)
            ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chat))
        }
    }

    @Auth
    @GetMapping("/chats")
    fun listChats(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
    ): ResponseEntity<ApiResponse<PagedThreadResponse>> {
        return ResponseEntity.ok(ApiResponse.success(chatService.listChats(currentPrincipal(), page, size, sort)))
    }

    @Auth
    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @PathVariable threadId: Long,
    ): ResponseEntity<ApiResponse<Unit>> {
        chatService.deleteThread(currentPrincipal(), threadId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    private fun currentPrincipal(): UserPrincipal =
        (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes)
            .request.getAttribute("principal") as UserPrincipal
}
