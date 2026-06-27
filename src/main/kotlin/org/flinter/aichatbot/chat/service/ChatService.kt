package org.flinter.aichatbot.chat.service

import org.flinter.aichatbot.chat.client.OpenAiClient
import org.flinter.aichatbot.chat.domain.Chat
import org.flinter.aichatbot.chat.domain.ChatThread
import org.flinter.aichatbot.chat.dto.request.CreateChatRequest
import org.flinter.aichatbot.chat.dto.response.ChatResponse
import org.flinter.aichatbot.chat.dto.response.PagedThreadResponse
import org.flinter.aichatbot.chat.dto.response.ThreadWithChatsResponse
import org.flinter.aichatbot.chat.repository.ChatRepository
import org.flinter.aichatbot.chat.repository.ThreadRepository
import org.flinter.aichatbot.chat.sse.SseHandler
import org.flinter.aichatbot.common.exception.AppException
import org.flinter.aichatbot.common.security.UserPrincipal
import org.flinter.aichatbot.user.domain.UserRole
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.OffsetDateTime

@Service
class ChatService(
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val openAiClient: OpenAiClient,
    private val sseHandler: SseHandler,
) {

    @Transactional
    fun createChat(principal: UserPrincipal, request: CreateChatRequest): ChatResponse {
        val thread = resolveOrCreateThread(principal.userId)
        val answer = openAiClient.callOpenAi(request.question, request.model)
        val chat = chatRepository.save(
            Chat(
                threadId = thread.id,
                question = request.question,
                answer = answer,
                model = request.model,
            )
        )
        return ChatResponse.from(chat)
    }

    @Transactional
    fun createChatStream(principal: UserPrincipal, request: CreateChatRequest): SseEmitter {
        val thread = resolveOrCreateThread(principal.userId)
        return sseHandler.stream { onChunk, onComplete ->
            openAiClient.streamOpenAi(
                question = request.question,
                model = request.model,
                onChunk = onChunk,
                onComplete = { fullAnswer ->
                    chatRepository.save(
                        Chat(
                            threadId = thread.id,
                            question = request.question,
                            answer = fullAnswer,
                            model = request.model,
                        )
                    )
                    onComplete()
                },
            )
        }
    }

    @Transactional(readOnly = true)
    fun listChats(principal: UserPrincipal, page: Int, size: Int, sort: String): PagedThreadResponse {
        val direction = if (sort.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"))

        val threadPage = if (principal.role == UserRole.ADMIN) {
            threadRepository.findAllByDeletedAtIsNull(pageable)
        } else {
            threadRepository.findAllByUserIdAndDeletedAtIsNull(principal.userId, pageable)
        }

        val threadIds = threadPage.content.map { it.id }
        val chatsByThread = chatRepository.findAllByThreadIdIn(threadIds)
            .groupBy { it.threadId }

        val threads = threadPage.content.map { thread ->
            ThreadWithChatsResponse(
                threadId = thread.id,
                createdAt = thread.createdAt,
                chats = (chatsByThread[thread.id] ?: emptyList()).map { ChatResponse.from(it) },
            )
        }

        return PagedThreadResponse(
            threads = threads,
            page = threadPage.number,
            size = threadPage.size,
            totalElements = threadPage.totalElements,
            totalPages = threadPage.totalPages,
        )
    }

    @Transactional
    fun deleteThread(principal: UserPrincipal, threadId: Long) {
        val thread = threadRepository.findById(threadId).orElseThrow {
            AppException(HttpStatus.NOT_FOUND, "Thread not found")
        }
        if (principal.role != UserRole.ADMIN && thread.userId != principal.userId) {
            throw AppException(HttpStatus.FORBIDDEN, "Access denied")
        }
        if (thread.deletedAt != null) {
            throw AppException(HttpStatus.NOT_FOUND, "Thread not found")
        }
        thread.deletedAt = OffsetDateTime.now()
        threadRepository.save(thread)
    }

    private fun resolveOrCreateThread(userId: Long): ChatThread {
        val recent = threadRepository.findFirstByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
        if (recent != null) {
            val lastChat = chatRepository.findTopByThreadIdOrderByCreatedAtDesc(recent.id)
            val lastActivityAt = lastChat?.createdAt ?: recent.createdAt
            if (lastActivityAt.isAfter(OffsetDateTime.now().minusMinutes(30))) {
                return recent
            }
        }
        return threadRepository.save(ChatThread(userId = userId))
    }
}
