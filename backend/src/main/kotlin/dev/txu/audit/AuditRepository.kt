package dev.txu.audit

import dev.txu.common.CorrelationFilter
import jakarta.servlet.http.HttpServletRequest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

data class AuditEventView(
    val id: UUID,
    val actorLabel: String,
    val eventType: String,
    val subjectId: UUID,
    val detail: String?,
    val occurredAt: Instant,
    val correlationId: String,
)

@Repository
class AuditRepository(
    private val jdbc: NamedParameterJdbcTemplate,
) {
    fun append(
        actorId: UUID?,
        actorLabel: String,
        eventType: String,
        subjectId: UUID,
        detail: String?,
        correlationId: String,
    ) {
        jdbc.update(
            """INSERT INTO audit_events(id, actor_id, actor_label, event_type, subject_id, detail, correlation_id)
               VALUES (:id, :actorId, :actorLabel, :eventType, :subjectId, :detail, :correlationId)""",
            mapOf(
                "id" to UUID.randomUUID(),
                "actorId" to actorId,
                "actorLabel" to actorLabel,
                "eventType" to eventType,
                "subjectId" to subjectId,
                "detail" to detail,
                "correlationId" to correlationId,
            ),
        )
    }

    fun recent(): List<AuditEventView> =
        jdbc.query(
            "SELECT * FROM audit_events ORDER BY occurred_at DESC LIMIT 100",
            emptyMap<String, Any>(),
        ) { result, _ ->
            AuditEventView(
                result.getObject("id", UUID::class.java),
                result.getString("actor_label"),
                result.getString("event_type"),
                result.getObject("subject_id", UUID::class.java),
                result.getString("detail"),
                result.getTimestamp("occurred_at").toInstant(),
                result.getString("correlation_id"),
            )
        }
}

fun correlationId(request: HttpServletRequest): String = request.getAttribute(CorrelationFilter.ATTRIBUTE)?.toString() ?: "unavailable"
