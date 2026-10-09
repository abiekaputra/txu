package dev.txu.identity

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

enum class AccountRole { MEMBER, MODERATOR, ADMIN }

data class Account(
    val id: UUID,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val role: AccountRole,
    val createdAt: Instant,
)

data class RegisterRequest(
    @field:NotBlank @field:Size(min = 2, max = 80) val displayName: String,
    @field:Email @field:Size(max = 254) val email: String,
    @field:Size(min = 12, max = 128) val password: String,
)

data class LoginRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String,
)

data class SessionUser(
    val id: UUID,
    val email: String,
    val displayName: String,
    val role: AccountRole,
)

data class SessionResponse(
    val authenticated: Boolean,
    val user: SessionUser?,
    val csrfToken: String,
)
