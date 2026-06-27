package org.flinter.aichatbot.chat.client

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.flinter.aichatbot.config.OpenAiProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.flinter.aichatbot.common.exception.AppException

@Component
class OpenAiClient(
    private val okHttpClient: OkHttpClient,
    private val props: OpenAiProperties,
    private val objectMapper: ObjectMapper,
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun callOpenAi(question: String, model: String): String {
        val body = objectMapper.writeValueAsString(
            mapOf(
                "model" to model,
                "messages" to listOf(mapOf("role" to "user", "content" to question)),
                "stream" to false,
            )
        )
        val request = Request.Builder()
            .url("${props.baseUrl}/v1/chat/completions")
            .header("Authorization", "Bearer ${props.apiKey}")
            .post(body.toRequestBody(jsonMediaType))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw AppException(HttpStatus.BAD_GATEWAY, "OpenAI API error: ${response.code}")
            }
            val json = objectMapper.readTree(response.body!!.string())
            return json["choices"][0]["message"]["content"].asText()
        }
    }

    fun streamOpenAi(
        question: String,
        model: String,
        onChunk: (String) -> Unit,
        onComplete: (String) -> Unit,
    ) {
        val body = objectMapper.writeValueAsString(
            mapOf(
                "model" to model,
                "messages" to listOf(mapOf("role" to "user", "content" to question)),
                "stream" to true,
            )
        )
        val request = Request.Builder()
            .url("${props.baseUrl}/v1/chat/completions")
            .header("Authorization", "Bearer ${props.apiKey}")
            .header("Accept", "text/event-stream")
            .post(body.toRequestBody(jsonMediaType))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw AppException(HttpStatus.BAD_GATEWAY, "OpenAI stream error: ${response.code}")
            }
            val fullAnswer = StringBuilder()
            response.body!!.source().use { source ->
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (!line.startsWith("data: ")) continue
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break
                    runCatching {
                        val json = objectMapper.readTree(data)
                        val chunk = json["choices"][0]["delta"]["content"]?.asText() ?: return@runCatching
                        if (chunk.isNotEmpty()) {
                            fullAnswer.append(chunk)
                            onChunk(chunk)
                        }
                    }
                }
            }
            onComplete(fullAnswer.toString())
        }
    }
}
