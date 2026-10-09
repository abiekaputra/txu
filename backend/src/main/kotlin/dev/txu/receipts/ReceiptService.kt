package dev.txu.receipts

import dev.txu.audit.AuditRepository
import dev.txu.common.ApiException
import dev.txu.identity.Account
import dev.txu.identity.IdentityService
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

@Service
class ReceiptService(
    private val receipts: ReceiptRepository,
    private val identity: IdentityService,
    private val canonicalizer: ReceiptCanonicalizer,
    private val signer: ReceiptSigner,
    private val audits: AuditRepository,
    private val metrics: MeterRegistry,
    @Value("\${txu.public-base-url}") private val publicBaseUrl: String,
    private val clock: Clock = Clock.systemUTC(),
) {
    fun list(authentication: Authentication): List<ReceiptView> = receipts.listOwned(actor(authentication).id).map { it.view() }

    @Transactional
    fun create(
        authentication: Authentication,
        request: DraftRequest,
    ): ReceiptView = receipts.createDraft(actor(authentication).id, request).view()

    fun get(
        authentication: Authentication,
        id: UUID,
    ): ReceiptView = owned(authentication, id).view()

    @Transactional
    fun update(
        authentication: Authentication,
        id: UUID,
        request: DraftRequest,
    ): ReceiptView {
        val actor = actor(authentication)
        if (receipts.updateDraft(id, actor.id, request) != 1) unavailableDraft()
        return receipts.findOwned(id, actor.id)!!.view()
    }

    @Transactional
    fun delete(
        authentication: Authentication,
        id: UUID,
    ) {
        if (receipts.deleteDraft(id, actor(authentication).id) != 1) unavailableDraft()
    }

    @Transactional
    fun issue(
        authentication: Authentication,
        id: UUID,
        correlationId: String,
    ): IssueView {
        val actor = actor(authentication)
        val draft = receipts.lockOwned(id, actor.id) ?: notFound()
        if (draft.lifecycle != ReceiptLifecycle.DRAFT) {
            return IssueView(draft.view(), publicUrl(draft.publicId!!), null)
        }

        val publicId = randomToken(18)
        val acknowledgementToken = randomToken(32)
        val issuedAt = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)
        val signedPayload =
            SignedPayload(
                publicId,
                signer.keyId,
                draft.issuerLabel,
                draft.recipientLabel,
                draft.title,
                draft.message,
                draft.category,
                issuedAt,
            )
        val canonical = canonicalizer.canonical(signedPayload)
        val bundle = signer.sign(canonical)
        check(receipts.issue(id, publicId, sha256(acknowledgementToken), canonical.toString(StandardCharsets.UTF_8), bundle, issuedAt) == 1)
        audits.append(actor.id, actor.displayName, "RECEIPT_ISSUED", id, "publicId=$publicId", correlationId)
        metrics.counter("txu.receipt.issue", "outcome", "success").increment()
        val issued = receipts.findOwned(id, actor.id) ?: error("Issued receipt could not be loaded")
        return IssueView(issued.view(), publicUrl(publicId), "$publicBaseUrl/a/$acknowledgementToken")
    }

    @Transactional
    fun revoke(
        authentication: Authentication,
        id: UUID,
        reason: String,
        correlationId: String,
    ): ReceiptView {
        val actor = actor(authentication)
        if (receipts.revoke(id, actor.id, normalize(reason)) != 1) {
            throw ApiException("RECEIPT_NOT_REVOCABLE", "Only an active issued receipt can be revoked.", HttpStatus.CONFLICT)
        }
        audits.append(actor.id, actor.displayName, "RECEIPT_REVOKED", id, normalize(reason), correlationId)
        return receipts.findOwned(id, actor.id)!!.view()
    }

    fun verify(publicId: String): VerificationView {
        val receipt = receipts.findPublic(publicId) ?: notFound()
        val view = verification(receipt)
        metrics.counter("txu.receipt.verification", "result", view.result.lowercase()).increment()
        return view
    }

    fun acknowledgement(token: String): AcknowledgementView {
        val receipt = receipts.findByAcknowledgementHash(sha256(token)) ?: notFound()
        return AcknowledgementView(eligible(receipt), acknowledgementState(receipt), verification(receipt))
    }

    @Transactional
    fun acknowledge(
        token: String,
        correlationId: String,
    ): AcknowledgementView {
        val receipt = receipts.findByAcknowledgementHash(sha256(token), lock = true) ?: notFound()
        if (receipt.acknowledgedAt == null && receipts.acknowledge(receipt.id) != 1) {
            throw ApiException("ACKNOWLEDGEMENT_UNAVAILABLE", "This receipt cannot be acknowledged.", HttpStatus.CONFLICT)
        }
        if (receipt.acknowledgedAt == null) {
            audits.append(null, "Recipient capability", "RECEIPT_ACKNOWLEDGED", receipt.id, null, correlationId)
        }
        val updated = receipts.findByAcknowledgementHash(sha256(token)) ?: error("Acknowledged receipt could not be loaded")
        return AcknowledgementView(false, "ACKNOWLEDGED", verification(updated))
    }

    private fun verification(receipt: ReceiptRow): VerificationView {
        if (receipt.moderationState == ModerationState.HIDDEN) return hidden(receipt)
        val valid = verifyIntegrity(receipt)
        val result =
            when {
                !valid -> "INVALID"
                receipt.lifecycle == ReceiptLifecycle.REVOKED -> "REVOKED"
                receipt.acknowledgedAt != null -> "VERIFIED_ACKNOWLEDGED"
                else -> "VERIFIED"
            }
        return VerificationView(
            result,
            if (valid) "VALID" else "INVALID",
            receipt.lifecycle,
            receipt.moderationState,
            receipt.acknowledgedAt,
            receipt.revokedAt,
            receipt.revocationReason,
            receipt.publicId,
            receipt.issuerLabel,
            receipt.recipientLabel,
            receipt.title,
            receipt.message,
            receipt.category,
            receipt.issuedAt,
            receipt.keyId,
            receipt.payloadSha256,
        )
    }

    private fun verifyIntegrity(receipt: ReceiptRow): Boolean {
        val publicId = receipt.publicId ?: return false
        val issuedAt = receipt.issuedAt ?: return false
        val keyId = receipt.keyId ?: return false
        val signature = receipt.signature ?: return false
        val storedPayload = receipt.canonicalPayload ?: return false
        val canonical =
            canonicalizer.canonical(
                SignedPayload(
                    publicId,
                    keyId,
                    receipt.issuerLabel,
                    receipt.recipientLabel,
                    receipt.title,
                    receipt.message,
                    receipt.category,
                    issuedAt,
                ),
            )
        return storedPayload == canonical.toString(StandardCharsets.UTF_8) &&
            receipt.payloadSha256 == sha256(canonical) && signer.verify(canonical, signature, keyId)
    }

    private fun hidden(receipt: ReceiptRow) =
        VerificationView(
            "HIDDEN",
            "NOT_DISCLOSED",
            receipt.lifecycle,
            receipt.moderationState,
            null,
            null,
            null,
            receipt.publicId,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
        )

    private fun eligible(receipt: ReceiptRow) =
        receipt.lifecycle == ReceiptLifecycle.ISSUED && receipt.moderationState != ModerationState.HIDDEN && receipt.acknowledgedAt == null

    private fun acknowledgementState(receipt: ReceiptRow) =
        when {
            receipt.acknowledgedAt != null -> "ACKNOWLEDGED"
            eligible(receipt) -> "PENDING"
            else -> "UNAVAILABLE"
        }

    private fun owned(
        authentication: Authentication,
        id: UUID,
    ) = receipts.findOwned(id, actor(authentication).id) ?: notFound()

    private fun actor(authentication: Authentication): Account = identity.actor(authentication.name)

    private fun publicUrl(publicId: String) = "$publicBaseUrl/r/$publicId"

    private fun randomToken(bytes: Int): String {
        val value = ByteArray(bytes)
        SecureRandom().nextBytes(value)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value)
    }

    private fun normalize(value: String) = value.trim().replace(Regex("\\s+"), " ")

    private fun unavailableDraft(): Nothing =
        throw ApiException("DRAFT_UNAVAILABLE", "The draft is missing or can no longer be changed.", HttpStatus.CONFLICT)

    private fun notFound(): Nothing = throw ApiException("RECEIPT_NOT_FOUND", "This receipt is unavailable.", HttpStatus.NOT_FOUND)
}
