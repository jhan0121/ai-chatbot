package org.flinter.aichatbot.auth.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다") val password: String,
    @field:NotBlank val name: String,
)
