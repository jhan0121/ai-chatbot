package org.flinter.aichatbot.common.security.aop

import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.flinter.aichatbot.common.exception.AppException
import org.flinter.aichatbot.common.security.UserPrincipal
import org.flinter.aichatbot.user.domain.UserRole
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class AuthAspect {

    @Before("@annotation(org.flinter.aichatbot.common.security.annotation.Auth)")
    fun checkAuth() {
        currentRequest().getAttribute("principal") as? UserPrincipal
            ?: throw AppException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다")
    }

    @Before("@annotation(org.flinter.aichatbot.common.security.annotation.Admin)")
    fun checkAdmin() {
        val principal = currentRequest().getAttribute("principal") as? UserPrincipal
            ?: throw AppException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다")
        if (principal.role != UserRole.ADMIN) {
            throw AppException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다")
        }
    }

    private fun currentRequest(): HttpServletRequest =
        (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
}
