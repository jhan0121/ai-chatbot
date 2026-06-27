package org.flinter.aichatbot.auth.service

import org.flinter.aichatbot.auth.dto.request.LoginRequest
import org.flinter.aichatbot.auth.dto.request.RegisterRequest
import org.flinter.aichatbot.auth.dto.response.TokenResponse
import org.flinter.aichatbot.common.exception.AppException
import org.flinter.aichatbot.common.security.PasswordEncoder
import org.flinter.aichatbot.security.jwt.JwtProvider
import org.flinter.aichatbot.user.domain.User
import org.flinter.aichatbot.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun register(request: RegisterRequest): Long {
        if (userRepository.existsByEmail(request.email)) {
            throw AppException(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다")
        }
        val user = userRepository.save(
            User(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                name = request.name,
            )
        )
        return user.id
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw AppException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다")
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw AppException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다")
        }
        return TokenResponse(accessToken = jwtProvider.generateToken(user.id, user.role))
    }
}
