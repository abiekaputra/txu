package dev.txu.common

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class AttemptGuard(
    private val clock: Clock = Clock.systemUTC(),
) {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()

    fun check(
        key: String,
        limit: Int,
        window: Duration,
    ) {
        val now = Instant.now(clock)
        val threshold = now.minus(window)
        val entries = attempts.computeIfAbsent(key) { mutableListOf() }
        synchronized(entries) {
            entries.removeIf { it.isBefore(threshold) }
            if (entries.size >= limit) {
                throw ApiException("RATE_LIMITED", "Too many attempts. Wait before trying again.", HttpStatus.TOO_MANY_REQUESTS)
            }
            entries.add(now)
        }
    }
}
