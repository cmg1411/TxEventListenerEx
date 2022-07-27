package com.example.txeventlistenerex.event

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import java.lang.IllegalArgumentException

private val logger = KotlinLogging.logger { }

data class TransactionEvent(private val payload: String)

@Component
class EventListener {

    @TransactionalEventListener
    fun onListen(event: TransactionEvent) {
        logger.info { "exception will be thrown !!" }
        throw IllegalArgumentException("예외가 발생했습니다.")
    }
}
