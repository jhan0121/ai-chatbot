package org.flinter.aichatbot.chat.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "chats")
class Chat(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false) val threadId: Long,
    @Column(nullable = false, columnDefinition = "TEXT") val question: String,
    @Column(nullable = false, columnDefinition = "TEXT") val answer: String,
    @Column(nullable = false) val model: String,
    @Column(nullable = false) val createdAt: OffsetDateTime = OffsetDateTime.now(),
)
