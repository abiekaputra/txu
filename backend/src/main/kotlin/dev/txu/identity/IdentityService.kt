package dev.txu.identity

import dev.txu.common.ApiException
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class IdentityService(
    private val accounts: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun register(request: RegisterRequest): Account {
        val email = request.email.trim().lowercase()
        if (accounts.existsByEmail(email)) {
            throw ApiException("EMAIL_UNAVAILABLE", "An account cannot be created with that email.", HttpStatus.CONFLICT)
        }
        return accounts.create(email, normalize(request.displayName), requireNotNull(passwordEncoder.encode(request.password)))
    }

    fun actor(email: String): Account =
        accounts.findByEmail(email.lowercase())
            ?: throw ApiException("SESSION_INVALID", "Sign in again to continue.", HttpStatus.UNAUTHORIZED)

    private fun normalize(value: String) = value.trim().replace(Regex("\\s+"), " ")
}

@Service
class TxuUserDetailsService(
    private val accounts: AccountRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val account = accounts.findByEmail(username.trim().lowercase()) ?: throw UsernameNotFoundException("Invalid credentials")
        return User
            .withUsername(account.email)
            .password(account.passwordHash)
            .roles(account.role.name)
            .build()
    }
}
