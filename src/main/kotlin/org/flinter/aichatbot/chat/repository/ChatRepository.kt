package org.flinter.aichatbot.chat.repository

import org.flinter.aichatbot.chat.domain.Chat
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {

    fun findAllByThreadIdIn(threadIds: List<Long>): List<Chat>

    fun findTopByThreadIdOrderByCreatedAtDesc(threadId: Long): Chat?
}
