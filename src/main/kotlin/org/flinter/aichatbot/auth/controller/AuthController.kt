package org.flinter.aichatbot.auth.controller

import jakarta.validation.Valid
import org.flinter.aichatbot.auth.dto.request.LoginRequest
import org.flinter.aichatbot.auth.dto.request.RegisterRequest
import org.flinter.aichatbot.auth.dto.response.TokenResponse
import org.flinter.aichatbot.auth.service.AuthService
import org.flinter.aichatbot.common.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): ResponseEntity<ApiResponse<Map<String, Long>>> {
        val userId = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(mapOf("userId" to userId)))
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<ApiResponse<TokenResponse>> {
        val token = authService.login(request)
        return ResponseEntity.ok(ApiResponse.success(token))
    }
}
