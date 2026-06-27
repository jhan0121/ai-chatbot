package org.flinter.aichatbot.security.jwt

import org.flinter.aichatbot.user.domain.UserRole
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("JwtProvider 단위 테스트")
class JwtProviderTest {

    private val secret = "dev-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm"
    private val jwtProvider = JwtProvider(secret = secret, expirationMs = 86400000L)

    @Test
    @DisplayName("토큰 생성 시 비어있지 않은 JWT 문자열 반환")
    fun generateTokenReturnsNonBlankJwt() {
        val token = jwtProvider.generateToken(userId = 1L, role = UserRole.MEMBER)
        assertTrue(token.isNotBlank())
        assertTrue(token.contains('.'))
    }

    @Test
    @DisplayName("방금 생성된 토큰은 유효함")
    fun validateTokenReturnsTrueForFreshToken() {
        val token = jwtProvider.generateToken(userId = 1L, role = UserRole.MEMBER)
        assertTrue(jwtProvider.validateToken(token))
    }

    @Test
    @DisplayName("변조된 토큰은 유효하지 않음")
    fun validateTokenReturnsFalseForTamperedToken() {
        val token = jwtProvider.generateToken(userId = 1L, role = UserRole.MEMBER)
        val tampered = token.dropLast(5) + "XXXXX"
        assertFalse(jwtProvider.validateToken(tampered))
    }

    @Test
    @DisplayName("빈 문자열은 유효하지 않음")
    fun validateTokenReturnsFalseForBlank() {
        assertFalse(jwtProvider.validateToken(""))
    }

    @Test
    @DisplayName("만료된 토큰은 유효하지 않음")
    fun validateTokenReturnsFalseForExpiredToken() {
        val expiredProvider = JwtProvider(secret = secret, expirationMs = -1L)
        val token = expiredProvider.generateToken(userId = 1L, role = UserRole.MEMBER)
        assertFalse(expiredProvider.validateToken(token))
    }

    @Test
    @DisplayName("토큰에서 userId 추출 성공")
    fun extractUserIdReturnsCorrectId() {
        val token = jwtProvider.generateToken(userId = 42L, role = UserRole.MEMBER)
        assertEquals(42L, jwtProvider.extractUserId(token))
    }

    @Test
    @DisplayName("토큰에서 역할 MEMBER 추출 성공")
    fun extractRoleReturnsMember() {
        val token = jwtProvider.generateToken(userId = 1L, role = UserRole.MEMBER)
        assertEquals(UserRole.MEMBER, jwtProvider.extractRole(token))
    }

    @Test
    @DisplayName("토큰에서 역할 ADMIN 추출 성공")
    fun extractRoleReturnsAdmin() {
        val token = jwtProvider.generateToken(userId = 1L, role = UserRole.ADMIN)
        assertEquals(UserRole.ADMIN, jwtProvider.extractRole(token))
    }
}
