package dev.txu.receipts

import dev.txu.audit.correlationId
import dev.txu.common.AttemptGuard
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.UUID

@RestController
@RequestMapping("/api/receipts")
class ReceiptController(
    private val service: ReceiptService,
) {
    @GetMapping
    fun list(authentication: Authentication) = service.list(authentication)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        authentication: Authentication,
        @Valid @RequestBody body: DraftRequest,
    ) = service.create(authentication, body)

    @GetMapping("/{id}")
    fun get(
        authentication: Authentication,
        @PathVariable id: UUID,
    ) = service.get(authentication, id)

    @PatchMapping("/{id}")
    fun update(
        authentication: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody body: DraftRequest,
    ) = service.update(authentication, id, body)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        authentication: Authentication,
        @PathVariable id: UUID,
    ) = service.delete(authentication, id)

    @PostMapping("/{id}/issue")
    fun issue(
        authentication: Authentication,
        @PathVariable id: UUID,
        request: HttpServletRequest,
    ) = service.issue(authentication, id, correlationId(request))

    @PostMapping("/{id}/revoke")
    fun revoke(
        authentication: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody body: RevokeRequest,
        request: HttpServletRequest,
    ) = service.revoke(authentication, id, body.reason, correlationId(request))
}

@RestController
class VerificationController(
    private val service: ReceiptService,
    private val attempts: AttemptGuard,
) {
    @GetMapping("/api/public/receipts/{publicId}")
    fun verify(
        @PathVariable publicId: String,
    ) = service.verify(publicId)

    @GetMapping("/api/acknowledgements/{token}")
    fun acknowledgement(
        @PathVariable token: String,
        request: HttpServletRequest,
    ): AcknowledgementView {
        guard(token, request)
        return service.acknowledgement(token)
    }

    @PostMapping("/api/acknowledgements/{token}")
    fun acknowledge(
        @PathVariable token: String,
        request: HttpServletRequest,
    ): AcknowledgementView {
        guard(token, request)
        return service.acknowledge(token, correlationId(request))
    }

    private fun guard(
        token: String,
        request: HttpServletRequest,
    ) {
        attempts.check("ack:${request.remoteAddr}:${sha256(token)}", 30, Duration.ofMinutes(15))
    }
}
