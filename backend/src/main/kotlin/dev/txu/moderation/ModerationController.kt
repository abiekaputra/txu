package dev.txu.moderation

import dev.txu.audit.correlationId
import dev.txu.common.AttemptGuard
import dev.txu.receipts.sha256
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.UUID

@RestController
class PublicReportController(
    private val service: ModerationService,
    private val attempts: AttemptGuard,
) {
    @PostMapping("/api/public/receipts/{publicId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    fun report(
        @PathVariable publicId: String,
        @Valid @RequestBody body: ReportRequest,
        request: HttpServletRequest,
    ): ReportView {
        attempts.check("report:${request.remoteAddr}:${sha256(publicId)}", 5, Duration.ofMinutes(15))
        return service.report(publicId, body, request.remoteAddr)
    }
}

@RestController
@RequestMapping("/api/moderation/reports")
class ModerationController(
    private val service: ModerationService,
) {
    @GetMapping
    fun list() = service.list()

    @PostMapping("/{id}/decision")
    fun decide(
        authentication: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody body: DecisionRequest,
        request: HttpServletRequest,
    ) = service.decide(authentication, id, body, correlationId(request))
}
