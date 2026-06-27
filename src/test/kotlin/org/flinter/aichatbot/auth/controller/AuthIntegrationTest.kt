package org.flinter.aichatbot.auth.controller

import org.flinter.aichatbot.auth.dto.request.LoginRequest
import org.flinter.aichatbot.auth.dto.request.RegisterRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Auth API 통합 테스트")
class AuthIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    @DisplayName("회원가입 성공 시 201과 userId 반환")
    fun registerReturns201WithUserId() {
        val request = RegisterRequest(
            email = "integration_test_1@example.com",
            password = "password123",
            name = "통합테스트",
        )
        val response = restTemplate.postForEntity("/api/auth/register", request, Map::class.java)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.body?.get("success") == true)
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 409 반환")
    fun registerReturns409ForDuplicateEmail() {
        val request = RegisterRequest(
            email = "integration_dup@example.com",
            password = "password123",
            name = "중복테스트",
        )
        restTemplate.postForEntity("/api/auth/register", request, Map::class.java)

        val response = restTemplate.postForEntity("/api/auth/register", request, Map::class.java)
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 400 반환")
    fun registerReturns400WhenPasswordTooShort() {
        val request = RegisterRequest(
            email = "shortpw@example.com",
            password = "1234567",
            name = "짧은비번",
        )
        val response = restTemplate.postForEntity("/api/auth/register", request, Map::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    @DisplayName("올바른 자격증명으로 로그인 시 accessToken 반환")
    fun loginReturnsAccessTokenForValidCredentials() {
        val email = "login_test@example.com"
        val password = "password123"
        restTemplate.postForEntity(
            "/api/auth/register",
            RegisterRequest(email = email, password = password, name = "로그인테스트"),
            Map::class.java,
        )

        val response = restTemplate.postForEntity(
            "/api/auth/login",
            LoginRequest(email = email, password = password),
            Map::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        val data = response.body?.get("data") as? Map<*, *>
        assertNotNull(data?.get("accessToken"))
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 반환")
    fun loginReturns401ForWrongPassword() {
        val email = "wrong_pw@example.com"
        restTemplate.postForEntity(
            "/api/auth/register",
            RegisterRequest(email = email, password = "correctpw", name = "틀린비번"),
            Map::class.java,
        )

        val response = restTemplate.postForEntity(
            "/api/auth/login",
            LoginRequest(email = email, password = "wrongpassword"),
            Map::class.java,
        )
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 401 반환")
    fun loginReturns401ForUnknownEmail() {
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            LoginRequest(email = "nobody@example.com", password = "password123"),
            Map::class.java,
        )
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }
}
