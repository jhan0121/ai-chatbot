package org.flinter.aichatbot.chat.repository

import jakarta.persistence.LockModeType
import org.flinter.aichatbot.chat.domain.ChatThread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface ThreadRepository : JpaRepository<ChatThread, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findFirstByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId: Long): ChatThread?

    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<ChatThread>

    fun findAllByUserIdAndDeletedAtIsNull(userId: Long, pageable: Pageable): Page<ChatThread>
}
