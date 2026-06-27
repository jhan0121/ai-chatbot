package org.flinter.aichatbot.config

import okhttp3.OkHttpClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@ConfigurationProperties(prefix = "openai")
data class OpenAiProperties(
    val apiKey: String = "",
    val baseUrl: String = "https://api.openai.com",
)

@Configuration
@EnableConfigurationProperties(OpenAiProperties::class)
class OpenAiConfig {

    @Bean
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
