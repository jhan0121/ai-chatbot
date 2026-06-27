package org.flinter.aichatbot.common.exception

import org.flinter.aichatbot.common.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AppException::class)
    fun handleAppException(e: AppException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(e.status)
            .body(ApiResponse.error(e.message ?: "오류가 발생했습니다"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val message = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ApiResponse.error(message))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("서버 내부 오류가 발생했습니다"))
}
