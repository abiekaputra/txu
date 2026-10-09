package dev.txu.receipts

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

data class SignedPayload(
    val publicId: String,
    val keyId: String,
    val issuerLabel: String,
    val recipientLabel: String,
    val title: String,
    val message: String,
    val category: ReceiptCategory,
    val issuedAt: Instant,
)

data class SignatureBundle(
    val signature: String,
    val payloadSha256: String,
    val keyId: String,
)

@Component
class ReceiptCanonicalizer {
    fun canonical(payload: SignedPayload): ByteArray {
        val fields =
            listOf(
                "schemaVersion" to "1",
                "publicId" to quote(payload.publicId),
                "keyId" to quote(payload.keyId),
                "issuerLabel" to quote(normalize(payload.issuerLabel)),
                "recipientLabel" to quote(normalize(payload.recipientLabel)),
                "title" to quote(normalize(payload.title)),
                "message" to quote(normalize(payload.message)),
                "category" to quote(payload.category.name),
                "issuedAt" to quote(payload.issuedAt.truncatedTo(ChronoUnit.MILLIS).toString()),
            )
        return fields
            .joinToString(prefix = "{", postfix = "}", separator = ",") { (key, value) -> "\"$key\":$value" }
            .toByteArray(StandardCharsets.UTF_8)
    }

    private fun normalize(value: String) = java.text.Normalizer.normalize(value.trim(), java.text.Normalizer.Form.NFC)

    private fun quote(value: String): String =
        buildString {
            append('"')
            value.forEach { character ->
                when (character) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> if (character.code < 32) append("\\u%04x".format(character.code)) else append(character)
                }
            }
            append('"')
        }
}

@Component
class ReceiptSigner(
    @Value("\${txu.signing.key-id}") val keyId: String,
    @Value("\${txu.signing.private-key-path}") private val privateKeyPath: String,
    @Value("\${txu.signing.public-key-path}") private val publicKeyPath: String,
    @Value("\${txu.signing.allow-ephemeral}") private val allowEphemeral: Boolean,
) {
    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey

    @PostConstruct
    fun loadKeys() {
        val keys = if (privateKeyPath.isNotBlank() && publicKeyPath.isNotBlank()) loadPair() else ephemeralPair()
        privateKey = keys.private
        publicKey = keys.public
    }

    fun sign(payload: ByteArray): SignatureBundle {
        val signer = Signature.getInstance("Ed25519")
        signer.initSign(privateKey)
        signer.update(payload)
        return SignatureBundle(Base64.getEncoder().encodeToString(signer.sign()), sha256(payload), keyId)
    }

    fun verify(
        payload: ByteArray,
        encodedSignature: String,
        storedKeyId: String,
    ): Boolean {
        if (storedKeyId != keyId) return false
        return runCatching {
            val verifier = Signature.getInstance("Ed25519")
            verifier.initVerify(publicKey)
            verifier.update(payload)
            verifier.verify(Base64.getDecoder().decode(encodedSignature))
        }.getOrDefault(false)
    }

    private fun loadPair(): KeyPair {
        val factory = KeyFactory.getInstance("Ed25519")
        val privateKey = factory.generatePrivate(PKCS8EncodedKeySpec(Files.readAllBytes(Path.of(privateKeyPath))))
        val publicKey = factory.generatePublic(X509EncodedKeySpec(Files.readAllBytes(Path.of(publicKeyPath))))
        return KeyPair(publicKey, privateKey)
    }

    private fun ephemeralPair(): KeyPair {
        check(allowEphemeral) { "Signing key files are required when ephemeral signing is disabled" }
        return KeyPairGenerator.getInstance("Ed25519").generateKeyPair()
    }
}

fun sha256(value: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(value).joinToString("") { "%02x".format(it) }

fun sha256(value: String): String = sha256(value.toByteArray(StandardCharsets.UTF_8))
