package org.flinter.aichatbot.auth.service

import org.flinter.aichatbot.auth.dto.request.LoginRequest
import org.flinter.aichatbot.auth.dto.request.RegisterRequest
import org.flinter.aichatbot.common.exception.AppException
import org.flinter.aichatbot.common.security.PasswordEncoder
import org.flinter.aichatbot.security.jwt.JwtProvider
import org.flinter.aichatbot.user.domain.User
import org.flinter.aichatbot.user.domain.UserRole
import org.flinter.aichatbot.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus

@ExtendWith(MockitoExtension::class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var jwtProvider: JwtProvider
    @Mock private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks private lateinit var authService: AuthService

    private fun buildUser(id: Long = 1L, role: UserRole = UserRole.MEMBER) = User(
        id = id,
        email = "user@example.com",
        password = "hashed-pw",
        name = "테스터",
        role = role,
    )

    @Test
    @DisplayName("이메일이 중복되지 않으면 회원가입 성공")
    fun registerSucceedsWhenEmailIsNotTaken() {
        val request = RegisterRequest(email = "new@example.com", password = "password1", name = "신규")
        `when`(userRepository.existsByEmail(request.email)).thenReturn(false)
        `when`(passwordEncoder.encode(request.password)).thenReturn("hashed")
        val savedUser = buildUser(id = 5L)
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)

        val result = authService.register(request)

        assertEquals(5L, result)
        verify(userRepository).save(any(User::class.java))
    }

    @Test
    @DisplayName("이메일이 이미 존재하면 409 반환")
    fun registerThrowsConflictWhenEmailAlreadyExists() {
        val request = RegisterRequest(email = "dup@example.com", password = "password1", name = "중복")
        `when`(userRepository.existsByEmail(request.email)).thenReturn(true)

        val ex = assertThrows(AppException::class.java) { authService.register(request) }
        assertEquals(HttpStatus.CONFLICT, ex.status)
    }

    @Test
    @DisplayName("올바른 자격증명으로 로그인 성공")
    fun loginReturnsTokenForValidCredentials() {
        val request = LoginRequest(email = "user@example.com", password = "plain-pw")
        val user = buildUser()
        `when`(userRepository.findByEmail(request.email)).thenReturn(user)
        `when`(passwordEncoder.matches(request.password, user.password)).thenReturn(true)
        `when`(jwtProvider.generateToken(user.id, user.role)).thenReturn("jwt-token")

        val result = authService.login(request)

        assertEquals("jwt-token", result.accessToken)
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 401 반환")
    fun loginThrowsUnauthorizedWhenEmailNotFound() {
        val request = LoginRequest(email = "unknown@example.com", password = "pw")
        `when`(userRepository.findByEmail(request.email)).thenReturn(null)

        val ex = assertThrows(AppException::class.java) { authService.login(request) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.status)
    }

    @Test
    @DisplayName("비밀번호가 틀리면 401 반환")
    fun loginThrowsUnauthorizedWhenPasswordDoesNotMatch() {
        val request = LoginRequest(email = "user@example.com", password = "wrong-pw")
        val user = buildUser()
        `when`(userRepository.findByEmail(request.email)).thenReturn(user)
        `when`(passwordEncoder.matches(request.password, user.password)).thenReturn(false)

        val ex = assertThrows(AppException::class.java) { authService.login(request) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.status)
    }
}
