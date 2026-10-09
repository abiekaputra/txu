package dev.txu.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class CorrelationFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val id = request.getHeader("X-Correlation-ID")?.takeIf { it.length in 8..80 } ?: UUID.randomUUID().toString()
        request.setAttribute(ATTRIBUTE, id)
        response.setHeader("X-Correlation-ID", id)
        MDC.put("correlationId", id)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("correlationId")
        }
    }

    companion object {
        const val ATTRIBUTE = "txu.correlationId"
    }
}
