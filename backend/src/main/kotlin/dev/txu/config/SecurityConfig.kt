package dev.txu.config

import dev.txu.identity.TxuUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        userDetails: TxuUserDetailsService,
        securityContexts: SecurityContextRepository,
    ): SecurityFilterChain {
        http
            .userDetailsService(userDetails)
            .securityContext { contexts -> contexts.securityContextRepository(securityContexts) }
            .csrf { csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/api/auth/**", "/api/session", "/api/public/**", "/api/acknowledgements/**")
                    .permitAll()
                    .requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/docs/**", "/docs.html")
                    .permitAll()
                    .requestMatchers("/api/moderation/**", "/api/audit/**")
                    .hasAnyRole("MODERATOR", "ADMIN")
                    .anyRequest()
                    .authenticated()
            }.sessionManagement { sessions -> sessions.sessionFixation { fixation -> fixation.migrateSession() } }
            .headers { headers ->
                headers
                    .contentSecurityPolicy { csp ->
                        csp.policyDirectives("default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'")
                    }.referrerPolicy { policy ->
                        policy.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                    }
            }.formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager = configuration.authenticationManager

    @Bean
    fun securityContextRepository(): SecurityContextRepository = HttpSessionSecurityContextRepository()
}
