package dev.txu.identity

import dev.txu.common.AttemptGuard
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
class SessionController(
    private val identity: IdentityService,
) {
    @GetMapping("/api/session")
    fun session(
        authentication: Authentication?,
        csrf: CsrfToken,
    ): SessionResponse {
        val user =
            authentication?.takeIf { it.isAuthenticated && it.name != "anonymousUser" }?.let { auth ->
                identity.actor(auth.name).view()
            }
        return SessionResponse(user != null, user, csrf.token)
    }
}

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val identity: IdentityService,
    private val authenticationManager: AuthenticationManager,
    private val securityContexts: SecurityContextRepository,
    private val attempts: AttemptGuard,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody body: RegisterRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SessionUser {
        attempts.check("register:${request.remoteAddr}", 10, Duration.ofMinutes(15))
        identity.register(body)
        return authenticate(body.email, body.password, request, response)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody body: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SessionUser {
        attempts.check("login:${request.remoteAddr}:${body.email.trim().lowercase()}", 12, Duration.ofMinutes(15))
        return authenticate(body.email, body.password, request, response)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(request: HttpServletRequest) {
        SecurityContextHolder.clearContext()
        request.getSession(false)?.invalidate()
    }

    private fun authenticate(
        email: String,
        password: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SessionUser {
        try {
            val authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(email.trim().lowercase(), password))
            val context = SecurityContextHolder.createEmptyContext()
            context.authentication = authentication
            SecurityContextHolder.setContext(context)
            securityContexts.saveContext(context, request, response)
            return identity.actor(authentication.name).view()
        } catch (error: BadCredentialsException) {
            throw dev.txu.common.ApiException("INVALID_CREDENTIALS", "Email or password is incorrect.", HttpStatus.UNAUTHORIZED)
        }
    }
}

private fun Account.view() = SessionUser(id, email, displayName, role)
