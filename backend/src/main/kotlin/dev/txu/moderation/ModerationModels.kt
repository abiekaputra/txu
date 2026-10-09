package dev.txu.moderation

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

enum class ReportReason { HARASSMENT, PERSONAL_INFORMATION, IMPERSONATION, OTHER }

enum class ReportStatus { OPEN, DISMISSED, RESOLVED }

enum class ModerationDecision { DISMISS, HIDE, RESTORE }

data class ReportRequest(
    val reason: ReportReason,
    @field:Size(max = 500) val details: String? = null,
)

data class DecisionRequest(
    val decision: ModerationDecision,
    @field:NotBlank @field:Size(max = 240) val reason: String,
)

data class ReportView(
    val id: UUID,
    val receiptId: UUID,
    val publicId: String,
    val receiptTitle: String,
    val recipientLabel: String,
    val issuerLabel: String,
    val reason: ReportReason,
    val details: String?,
    val status: ReportStatus,
    val moderationState: String,
    val decisionReason: String?,
    val createdAt: Instant,
    val decidedAt: Instant?,
)
