package org.flinter.aichatbot.user.domain

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.MEMBER,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
)
