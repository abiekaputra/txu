package dev.txu.common

import org.springframework.http.HttpStatus

class ApiException(
    val code: String,
    override val message: String,
    val status: HttpStatus,
) : RuntimeException(message)
