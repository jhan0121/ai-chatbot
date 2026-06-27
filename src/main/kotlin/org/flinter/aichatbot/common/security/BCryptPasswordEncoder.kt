package org.flinter.aichatbot.common.security

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

@Component
class BCryptPasswordEncoder : PasswordEncoder {
    override fun encode(rawPassword: String): String = BCrypt.hashpw(rawPassword, BCrypt.gensalt())
    override fun matches(rawPassword: String, encodedPassword: String): Boolean = BCrypt.checkpw(rawPassword, encodedPassword)
}
