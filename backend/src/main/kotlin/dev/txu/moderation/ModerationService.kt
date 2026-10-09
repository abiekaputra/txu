package dev.txu.moderation

import dev.txu.audit.AuditRepository
import dev.txu.common.ApiException
import dev.txu.identity.IdentityService
import dev.txu.receipts.ModerationState
import dev.txu.receipts.ReceiptRepository
import dev.txu.receipts.sha256
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ModerationService(
    private val reports: ModerationRepository,
    private val receipts: ReceiptRepository,
    private val identity: IdentityService,
    private val audits: AuditRepository,
    @Value("\${txu.report-fingerprint-salt}") private val fingerprintSalt: String,
) {
    @Transactional
    fun report(
        publicId: String,
        request: ReportRequest,
        remoteAddress: String,
    ): ReportView {
        val receipt = receipts.findPublic(publicId) ?: notFound()
        if (receipt.moderationState == ModerationState.HIDDEN) notFound()
        val fingerprint = sha256("$fingerprintSalt:$remoteAddress:${request.reason.name}")
        if (reports.duplicateExists(receipt.id, fingerprint)) {
            throw ApiException("REPORT_DUPLICATE", "A similar report was already received.", HttpStatus.CONFLICT)
        }
        receipts.setModeration(receipt.id, ModerationState.FLAGGED, null)
        return reports.create(receipt.id, request, fingerprint)
    }

    fun list(): List<ReportView> = reports.list()

    @Transactional
    fun decide(
        authentication: Authentication,
        id: UUID,
        request: DecisionRequest,
        correlationId: String,
    ): ReportView {
        val actor = identity.actor(authentication.name)
        val report = reports.find(id, lock = true) ?: notFound()
        val reason = request.reason.trim().replace(Regex("\\s+"), " ")
        when (request.decision) {
            ModerationDecision.DISMISS -> {
                reports.decide(id, ReportStatus.DISMISSED, reason)
                if (!reports.hasOpen(report.receiptId)) receipts.setModeration(report.receiptId, ModerationState.CLEAR, null)
            }

            ModerationDecision.HIDE -> {
                reports.decide(id, ReportStatus.RESOLVED, reason)
                receipts.setModeration(report.receiptId, ModerationState.HIDDEN, reason)
            }

            ModerationDecision.RESTORE -> {
                reports.decide(id, ReportStatus.RESOLVED, reason)
                receipts.setModeration(report.receiptId, ModerationState.CLEAR, reason)
            }
        }
        audits.append(actor.id, actor.displayName, "MODERATION_${request.decision.name}", report.receiptId, reason, correlationId)
        return reports.find(id) ?: error("Moderated report could not be loaded")
    }

    private fun notFound(): Nothing = throw ApiException("REPORT_NOT_FOUND", "That moderation record is unavailable.", HttpStatus.NOT_FOUND)
}
