package org.flinter.aichatbot.chat.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "threads")
class ChatThread(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false) val userId: Long,
    @Column(nullable = false) val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column var deletedAt: OffsetDateTime? = null,
)
