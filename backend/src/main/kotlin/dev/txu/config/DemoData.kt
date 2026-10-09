package dev.txu.config

import dev.txu.identity.AccountRepository
import dev.txu.identity.AccountRole
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
@Profile("demo")
class DemoData(
    private val accounts: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @PostConstruct
    fun seed() {
        if (!accounts.existsByEmail("moderator@txu.local")) {
            accounts.create(
                email = "moderator@txu.local",
                displayName = "Local Moderator",
                passwordHash = requireNotNull(passwordEncoder.encode("local-txu-moderator")),
                role = AccountRole.MODERATOR,
            )
        }
    }
}
