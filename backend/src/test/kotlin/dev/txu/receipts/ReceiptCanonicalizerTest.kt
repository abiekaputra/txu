package dev.txu.receipts

import java.nio.charset.StandardCharsets
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ReceiptCanonicalizerTest {
    private val canonicalizer = ReceiptCanonicalizer()

    @Test
    fun `canonical payload has fixed order and normalized timestamp`() {
        val bytes = canonicalizer.canonical(payload())
        val expected =
            """{"schemaVersion":1,"publicId":"public-1","keyId":"test-key","issuerLabel":"Ari","recipientLabel":"Maya","title":"A careful review","message":"Thank you for explaining the failure.\nIt helped everyone.","category":"TEAMWORK","issuedAt":"2026-10-09T04:00:00.123Z"}"""
        assertEquals(expected, bytes.toString(StandardCharsets.UTF_8))
        assertContentEquals(bytes, canonicalizer.canonical(payload()))
    }

    private fun payload() =
        SignedPayload(
            publicId = "public-1",
            keyId = "test-key",
            issuerLabel = " Ari ",
            recipientLabel = "Maya",
            title = "A careful review",
            message = "Thank you for explaining the failure.\nIt helped everyone.",
            category = ReceiptCategory.TEAMWORK,
            issuedAt = Instant.parse("2026-10-09T04:00:00.123456Z"),
        )
}
