package dev.txu.receipts

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReceiptSignerTest {
    @Test
    fun `signature verifies original bytes and rejects tampering`() {
        val signer = ReceiptSigner("test-key", "", "", true)
        signer.loadKeys()
        val original = "immutable receipt".toByteArray()
        val signed = signer.sign(original)

        assertTrue(signer.verify(original, signed.signature, signed.keyId))
        assertFalse(signer.verify("changed receipt".toByteArray(), signed.signature, signed.keyId))
        assertFalse(signer.verify(original, signed.signature, "other-key"))
    }
}
