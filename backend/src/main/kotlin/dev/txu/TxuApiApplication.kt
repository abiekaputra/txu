package dev.txu

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TxuApiApplication

fun main(args: Array<String>) {
    runApplication<TxuApiApplication>(*args)
}
