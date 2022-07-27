package com.example.txeventlistenerex.event

import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.transaction.event.*
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.lang.reflect.Method

@Configuration
class AdaptorConfig {
    @Bean
    fun transactionalEventListenerFactory(): TransactionalEventListenerFactory {
        return CustomTransactionalEventListenerFactory()
    }
}

class CustomTransactionalEventListenerFactory : TransactionalEventListenerFactory(), Ordered {

    override fun createApplicationListener(beanName: String, type: Class<*>, method: Method): ApplicationListener<*> {
        return CustomTransactionalApplicationListenerMethodAdapter(beanName, type, method)
    }
}

class CustomTransactionalApplicationListenerMethodAdapter(
    beanName: String,
    targetClass: Class<*>,
    method: Method
) : TransactionalApplicationListenerMethodAdapter(beanName, targetClass, method) {

    override fun onApplicationEvent(event: ApplicationEvent) {
        val superClassCallback = this.javaClass.superclass.getDeclaredField("callbacks").apply { this.isAccessible = true }.get(this)
                as List<TransactionalApplicationListener.SynchronizationCallback>
        val superClassAnnotation = this.javaClass.superclass.getDeclaredField("annotation").apply { this.isAccessible = true }.get(this)
                as TransactionalEventListener

        if (TransactionSynchronizationManager.isSynchronizationActive() &&
            TransactionSynchronizationManager.isActualTransactionActive()
        ) {
            TransactionSynchronizationManager.registerSynchronization(
                CustomTransactionalApplicationListenerSynchronization(event, this, superClassCallback)
            )
        } else if (superClassAnnotation.fallbackExecution) {
            if (superClassAnnotation.phase == TransactionPhase.AFTER_ROLLBACK && logger.isWarnEnabled) {
                logger.warn("Processing $event as a fallback execution on AFTER_ROLLBACK phase")
            }
            processEvent(event)
        } else {
            // No transactional event execution at all
            if (logger.isDebugEnabled) {
                logger.debug("No transaction is active - skipping $event")
            }
        }
    }
}

class CustomTransactionalApplicationListenerSynchronization<E : ApplicationEvent>(
    private val event: E,
    private val listener: TransactionalApplicationListener<E>,
    private val callbacks: List<TransactionalApplicationListener.SynchronizationCallback>
) : TransactionSynchronization {

    override fun getOrder(): Int {
        return listener.order
    }

    override fun beforeCommit(readOnly: Boolean) {
        if (listener.transactionPhase == TransactionPhase.BEFORE_COMMIT) {
            processEventWithCallbacks()
        }
    }

    // 이 부분을 변경한 커스텀 클래스
    override fun afterCommit() {
        val phase = listener.transactionPhase
        if (phase == TransactionPhase.AFTER_COMMIT) {
            processEventWithCallbacks()
        }
    }

    override fun afterCompletion(status: Int) {
        val phase = listener.transactionPhase
         if (phase == TransactionPhase.AFTER_ROLLBACK && status == TransactionSynchronization.STATUS_ROLLED_BACK) {
            processEventWithCallbacks()
        } else if (phase == TransactionPhase.AFTER_COMPLETION) {
            processEventWithCallbacks()
        }
    }

    private fun processEventWithCallbacks() {
        this.callbacks.forEach { it.preProcessEvent(this.event) }

        try {
            this.listener.processEvent(event)
        } catch (ex: RuntimeException) {
            this.callbacks.forEach { it.postProcessEvent(this.event, ex) }
            throw ex
        } catch (ex: Error) {
            this.callbacks.forEach { it.postProcessEvent(this.event, ex) }
            throw ex
        }

        callbacks.forEach { it.postProcessEvent(event, null) }
    }
}