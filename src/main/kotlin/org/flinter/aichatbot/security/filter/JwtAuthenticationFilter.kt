package org.flinter.aichatbot.security.filter

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.flinter.aichatbot.common.dto.ApiResponse
import org.flinter.aichatbot.common.security.UserPrincipal
import org.flinter.aichatbot.security.jwt.JwtProvider
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI.startsWith("/api/auth/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractToken(request)

        if (token != null) {
            if (!jwtProvider.validateToken(token)) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.contentType = "application/json;charset=UTF-8"
                response.writer.write(objectMapper.writeValueAsString(ApiResponse.error("유효하지 않은 토큰입니다")))
                return
            }
            request.setAttribute(
                "principal",
                UserPrincipal(
                    userId = jwtProvider.extractUserId(token),
                    role = jwtProvider.extractRole(token),
                ),
            )
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ")) return null
        return header.substring(7)
    }
}
