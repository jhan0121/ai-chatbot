package org.flinter.aichatbot.common.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
) {
    companion object {
        fun <T> success(data: T) = ApiResponse(success = true, data = data, message = null)
        fun error(message: String) = ApiResponse<Nothing>(success = false, data = null, message = message)
    }
}
