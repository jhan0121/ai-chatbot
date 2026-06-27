package org.flinter.aichatbot.common.exception

import org.springframework.http.HttpStatus

class AppException(
    val status: HttpStatus,
    message: String,
) : RuntimeException(message)
