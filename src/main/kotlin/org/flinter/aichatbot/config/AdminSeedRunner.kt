package org.flinter.aichatbot.config

import org.flinter.aichatbot.common.security.PasswordEncoder
import org.flinter.aichatbot.user.domain.User
import org.flinter.aichatbot.user.domain.UserRole
import org.flinter.aichatbot.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class AdminSeedRunner(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.admin.email}") private val adminEmail: String,
    @Value("\${app.admin.password}") private val adminPassword: String,
    @Value("\${app.admin.name}") private val adminName: String,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (!userRepository.existsByEmail(adminEmail)) {
            userRepository.save(
                User(
                    email = adminEmail,
                    password = passwordEncoder.encode(adminPassword),
                    name = adminName,
                    role = UserRole.ADMIN,
                )
            )
        }
    }
}
