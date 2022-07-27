package com.example.txeventlistenerex.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.transaction.ChainedTransactionManager
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ChainedTransactionManagerConfig {

    @Bean
    fun chainedTransactionManager(
        @Qualifier("h2TransactionManager") h2TransactionManager: PlatformTransactionManager,
        @Qualifier("mysqlTransactionManager") mysqlTransactionManager: PlatformTransactionManager
    ) = ChainedTransactionManager(h2TransactionManager, mysqlTransactionManager)
}