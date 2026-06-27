package org.flinter.aichatbot.common.security

import org.flinter.aichatbot.user.domain.UserRole

data class UserPrincipal(
    val userId: Long,
    val role: UserRole,
)
