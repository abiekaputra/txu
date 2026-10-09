package dev.txu.moderation

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class ModerationRepository(
    private val jdbc: NamedParameterJdbcTemplate,
) {
    fun duplicateExists(
        receiptId: UUID,
        fingerprint: String,
    ): Boolean =
        jdbc.queryForObject(
            """SELECT EXISTS(SELECT 1 FROM reports WHERE receipt_id = :receiptId AND fingerprint = :fingerprint
               AND created_at > now() - interval '1 hour')""",
            mapOf("receiptId" to receiptId, "fingerprint" to fingerprint),
            Boolean::class.java,
        ) == true

    fun create(
        receiptId: UUID,
        request: ReportRequest,
        fingerprint: String,
    ): ReportView {
        val id = UUID.randomUUID()
        jdbc.update(
            """INSERT INTO reports(id, receipt_id, reason, details, fingerprint)
               VALUES (:id, :receiptId, :reason, :details, :fingerprint)""",
            mapOf(
                "id" to id,
                "receiptId" to receiptId,
                "reason" to request.reason.name,
                "details" to request.details?.trim()?.takeIf { it.isNotEmpty() },
                "fingerprint" to fingerprint,
            ),
        )
        return find(id) ?: error("Created report could not be loaded")
    }

    fun list(): List<ReportView> =
        jdbc.query(
            BASE_QUERY + " ORDER BY CASE WHEN p.status = 'OPEN' THEN 0 ELSE 1 END, p.created_at DESC",
            emptyMap<String, Any>(),
            ::map,
        )

    fun find(
        id: UUID,
        lock: Boolean = false,
    ): ReportView? {
        val suffix = if (lock) " FOR UPDATE OF p" else ""
        return jdbc.query(BASE_QUERY + " WHERE p.id = :id$suffix", mapOf("id" to id), ::map).firstOrNull()
    }

    fun decide(
        id: UUID,
        status: ReportStatus,
        reason: String,
    ): Int =
        jdbc.update(
            "UPDATE reports SET status = :status, decision_reason = :reason, decided_at = now() WHERE id = :id",
            mapOf("id" to id, "status" to status.name, "reason" to reason),
        )

    fun hasOpen(receiptId: UUID): Boolean =
        jdbc.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM reports WHERE receipt_id = :receiptId AND status = 'OPEN')",
            mapOf("receiptId" to receiptId),
            Boolean::class.java,
        ) == true

    private fun map(
        result: ResultSet,
        row: Int,
    ) = ReportView(
        result.getObject("id", UUID::class.java),
        result.getObject("receipt_id", UUID::class.java),
        result.getString("public_id"),
        result.getString("receipt_title"),
        result.getString("recipient_label"),
        result.getString("issuer_label"),
        ReportReason.valueOf(result.getString("reason")),
        result.getString("details"),
        ReportStatus.valueOf(result.getString("status")),
        result.getString("moderation_state"),
        result.getString("decision_reason"),
        result.getTimestamp("created_at").toInstant(),
        result.getTimestamp("decided_at")?.toInstant(),
    )

    companion object {
        private const val BASE_QUERY =
            """SELECT p.*, r.public_id, r.title AS receipt_title, r.recipient_label, r.moderation_state,
               a.display_name AS issuer_label FROM reports p JOIN receipts r ON r.id = p.receipt_id
               JOIN accounts a ON a.id = r.issuer_id"""
    }
}
