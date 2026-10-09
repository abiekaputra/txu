package dev.txu.audit

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/audit")
class AuditController(
    private val audits: AuditRepository,
) {
    @GetMapping
    fun recent() = audits.recent()
}
