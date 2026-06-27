package org.flinter.aichatbot.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.flinter.aichatbot.user.domain.UserRole
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.expiration-ms}") private val expirationMs: Long,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(userId: Long, role: UserRole): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role.name)
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun extractUserId(token: String): Long {
        return getClaims(token).subject.toLong()
    }

    fun extractRole(token: String): UserRole {
        return UserRole.valueOf(getClaims(token).get("role", String::class.java))
    }

    private fun getClaims(token: String) =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}
