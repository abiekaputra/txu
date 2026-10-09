package dev.txu.receipts

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Repository
class ReceiptRepository(
    private val jdbc: NamedParameterJdbcTemplate,
) {
    fun createDraft(
        issuerId: UUID,
        request: DraftRequest,
    ): ReceiptRow {
        val id = UUID.randomUUID()
        jdbc.update(
            """INSERT INTO receipts(id, issuer_id, recipient_label, title, message, category)
               VALUES (:id, :issuerId, :recipient, :title, :message, :category)""",
            fields(request) + mapOf("id" to id, "issuerId" to issuerId),
        )
        return findOwned(id, issuerId) ?: error("Created draft could not be loaded")
    }

    fun listOwned(issuerId: UUID): List<ReceiptRow> =
        jdbc.query(BASE_QUERY + " WHERE r.issuer_id = :issuerId ORDER BY r.updated_at DESC", mapOf("issuerId" to issuerId), ::map)

    fun findOwned(
        id: UUID,
        issuerId: UUID,
    ): ReceiptRow? =
        jdbc
            .query(
                BASE_QUERY + " WHERE r.id = :id AND r.issuer_id = :issuerId",
                mapOf("id" to id, "issuerId" to issuerId),
                ::map,
            ).firstOrNull()

    fun lockOwned(
        id: UUID,
        issuerId: UUID,
    ): ReceiptRow? =
        jdbc
            .query(
                BASE_QUERY + " WHERE r.id = :id AND r.issuer_id = :issuerId FOR UPDATE",
                mapOf("id" to id, "issuerId" to issuerId),
                ::map,
            ).firstOrNull()

    fun findPublic(publicId: String): ReceiptRow? =
        jdbc.query(BASE_QUERY + " WHERE r.public_id = :publicId", mapOf("publicId" to publicId), ::map).firstOrNull()

    fun findByAcknowledgementHash(
        hash: String,
        lock: Boolean = false,
    ): ReceiptRow? {
        val suffix = if (lock) " FOR UPDATE" else ""
        return jdbc.query(BASE_QUERY + " WHERE r.acknowledgement_hash = :hash$suffix", mapOf("hash" to hash), ::map).firstOrNull()
    }

    fun updateDraft(
        id: UUID,
        issuerId: UUID,
        request: DraftRequest,
    ): Int =
        jdbc.update(
            """UPDATE receipts SET recipient_label = :recipient, title = :title, message = :message,
               category = :category, updated_at = now(), version = version + 1
               WHERE id = :id AND issuer_id = :issuerId AND lifecycle = 'DRAFT'""",
            fields(request) + mapOf("id" to id, "issuerId" to issuerId),
        )

    fun deleteDraft(
        id: UUID,
        issuerId: UUID,
    ): Int =
        jdbc.update(
            "DELETE FROM receipts WHERE id = :id AND issuer_id = :issuerId AND lifecycle = 'DRAFT'",
            mapOf(
                "id" to id,
                "issuerId" to issuerId,
            ),
        )

    fun issue(
        id: UUID,
        publicId: String,
        tokenHash: String,
        payload: String,
        bundle: SignatureBundle,
        issuedAt: Instant,
    ): Int =
        jdbc.update(
            """UPDATE receipts SET lifecycle = 'ISSUED', public_id = :publicId, schema_version = 1,
               key_id = :keyId, issued_at = :issuedAt, canonical_payload = :payload,
               payload_sha256 = :payloadHash, signature = :signature,
               acknowledgement_hash = :tokenHash, updated_at = now(), version = version + 1
               WHERE id = :id AND lifecycle = 'DRAFT'""",
            mapOf(
                "id" to id,
                "publicId" to publicId,
                "tokenHash" to tokenHash,
                "payload" to payload,
                "keyId" to bundle.keyId,
                "payloadHash" to bundle.payloadSha256,
                "signature" to bundle.signature,
                "issuedAt" to Timestamp.from(issuedAt),
            ),
        )

    fun acknowledge(id: UUID): Int =
        jdbc.update(
            """UPDATE receipts SET acknowledged_at = now(), updated_at = now(), version = version + 1
               WHERE id = :id AND lifecycle = 'ISSUED' AND moderation_state <> 'HIDDEN' AND acknowledged_at IS NULL""",
            mapOf("id" to id),
        )

    fun revoke(
        id: UUID,
        issuerId: UUID,
        reason: String,
    ): Int =
        jdbc.update(
            """UPDATE receipts SET lifecycle = 'REVOKED', revoked_at = now(), revocation_reason = :reason,
               updated_at = now(), version = version + 1
               WHERE id = :id AND issuer_id = :issuerId AND lifecycle = 'ISSUED'""",
            mapOf("id" to id, "issuerId" to issuerId, "reason" to reason),
        )

    fun setModeration(
        id: UUID,
        state: ModerationState,
        reason: String?,
    ): Int =
        jdbc.update(
            "UPDATE receipts SET moderation_state = :state, moderation_reason = :reason, updated_at = now() WHERE id = :id",
            mapOf("id" to id, "state" to state.name, "reason" to reason),
        )

    private fun fields(request: DraftRequest) =
        mapOf(
            "recipient" to normalize(request.recipientLabel),
            "title" to normalize(request.title),
            "message" to normalize(request.message),
            "category" to request.category.name,
        )

    private fun normalize(value: String) = value.trim().replace(Regex("[ \\t]+"), " ")

    private fun map(
        result: ResultSet,
        row: Int,
    ) = ReceiptRow(
        id = result.getObject("id", UUID::class.java),
        issuerId = result.getObject("issuer_id", UUID::class.java),
        issuerLabel = result.getString("issuer_label"),
        recipientLabel = result.getString("recipient_label"),
        title = result.getString("title"),
        message = result.getString("message"),
        category = ReceiptCategory.valueOf(result.getString("category")),
        lifecycle = ReceiptLifecycle.valueOf(result.getString("lifecycle")),
        moderationState = ModerationState.valueOf(result.getString("moderation_state")),
        publicId = result.getString("public_id"),
        schemaVersion = result.getObject("schema_version") as Int?,
        keyId = result.getString("key_id"),
        issuedAt = result.getTimestamp("issued_at")?.toInstant(),
        canonicalPayload = result.getString("canonical_payload"),
        payloadSha256 = result.getString("payload_sha256"),
        signature = result.getString("signature"),
        acknowledgedAt = result.getTimestamp("acknowledged_at")?.toInstant(),
        revokedAt = result.getTimestamp("revoked_at")?.toInstant(),
        revocationReason = result.getString("revocation_reason"),
        moderationReason = result.getString("moderation_reason"),
        createdAt = result.getTimestamp("created_at").toInstant(),
        updatedAt = result.getTimestamp("updated_at").toInstant(),
    )

    companion object {
        private const val BASE_QUERY =
            """SELECT r.*, a.display_name AS issuer_label FROM receipts r
               JOIN accounts a ON a.id = r.issuer_id"""
    }
}
