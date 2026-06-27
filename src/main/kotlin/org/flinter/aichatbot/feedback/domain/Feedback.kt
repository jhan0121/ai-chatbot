package org.flinter.aichatbot.feedback.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "feedbacks")
class Feedback(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false) val userId: Long,
    @Column(nullable = false) val chatId: Long,
    @Column(nullable = false) val positive: Boolean,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) var status: FeedbackStatus = FeedbackStatus.PENDING,
    @Column(nullable = false) val createdAt: OffsetDateTime = OffsetDateTime.now(),
)
