package dev.txu.receipts

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

enum class ReceiptCategory { MENTORSHIP, TEAMWORK, SUPPORT, CRAFT, OTHER }

enum class ReceiptLifecycle { DRAFT, ISSUED, REVOKED }

enum class ModerationState { CLEAR, FLAGGED, HIDDEN }

data class ReceiptRow(
    val id: UUID,
    val issuerId: UUID,
    val issuerLabel: String,
    val recipientLabel: String,
    val title: String,
    val message: String,
    val category: ReceiptCategory,
    val lifecycle: ReceiptLifecycle,
    val moderationState: ModerationState,
    val publicId: String?,
    val schemaVersion: Int?,
    val keyId: String?,
    val issuedAt: Instant?,
    val canonicalPayload: String?,
    val payloadSha256: String?,
    val signature: String?,
    val acknowledgedAt: Instant?,
    val revokedAt: Instant?,
    val revocationReason: String?,
    val moderationReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class DraftRequest(
    @field:NotBlank @field:Size(min = 2, max = 80) val recipientLabel: String,
    @field:NotBlank @field:Size(min = 5, max = 80) val title: String,
    @field:NotBlank @field:Size(min = 20, max = 1000) val message: String,
    @field:NotNull val category: ReceiptCategory,
)

data class RevokeRequest(
    @field:NotBlank @field:Size(max = 240) val reason: String,
)

data class ReceiptView(
    val id: UUID,
    val recipientLabel: String,
    val title: String,
    val message: String,
    val category: ReceiptCategory,
    val lifecycle: ReceiptLifecycle,
    val moderationState: ModerationState,
    val publicId: String?,
    val issuedAt: Instant?,
    val acknowledgedAt: Instant?,
    val revokedAt: Instant?,
    val revocationReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class IssueView(
    val receipt: ReceiptView,
    val publicUrl: String,
    val recipientUrl: String?,
)

data class VerificationView(
    val result: String,
    val integrity: String,
    val lifecycle: ReceiptLifecycle?,
    val moderationState: ModerationState?,
    val acknowledgedAt: Instant?,
    val revokedAt: Instant?,
    val revocationReason: String?,
    val publicId: String?,
    val issuerLabel: String?,
    val recipientLabel: String?,
    val title: String?,
    val message: String?,
    val category: ReceiptCategory?,
    val issuedAt: Instant?,
    val keyId: String?,
    val payloadSha256: String?,
)

data class AcknowledgementView(
    val eligible: Boolean,
    val state: String,
    val receipt: VerificationView,
)

fun ReceiptRow.view() =
    ReceiptView(
        id,
        recipientLabel,
        title,
        message,
        category,
        lifecycle,
        moderationState,
        publicId,
        issuedAt,
        acknowledgedAt,
        revokedAt,
        revocationReason,
        createdAt,
        updatedAt,
    )
