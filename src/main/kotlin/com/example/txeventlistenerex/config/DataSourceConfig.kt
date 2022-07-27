package com.example.txeventlistenerex.config

import org.springframework.core.env.Environment
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.PropertySource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@PropertySource(value = ["classpath:application.yml"])
@EnableJpaRepositories(
    basePackages = ["com.example.txeventlistenerex.entity.h2"],
    entityManagerFactoryRef = "h2EntityManager",
    transactionManagerRef = "h2TransactionManager"
)
class DataSourceConfig(
    private val env: Environment
) {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.h2")
    fun h2DataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean
    @Primary
    fun h2EntityManager(): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = h2DataSource()
            this.jpaVendorAdapter = HibernateJpaVendorAdapter().apply { setDatabase(Database.H2) }
            this.setJpaPropertyMap(mapOf("hibernate.hbm2ddl.auto" to env.getProperty("spring.jpa.hibernate.ddl-auto")))
            this.setPackagesToScan("com.example.txeventlistenerex.entity.h2")
        }
    }

    @Bean
    @Primary
    fun h2TransactionManager(): PlatformTransactionManager {
        return JpaTransactionManager().apply {
            this.entityManagerFactory = h2EntityManager().`object`
        }
    }
}

@Configuration
@PropertySource(value = ["classpath:application.yml"])
@EnableJpaRepositories(
    basePackages = ["com.example.txeventlistenerex.entity.mysql"],
    entityManagerFactoryRef = "mysqlEntityManager",
    transactionManagerRef = "mysqlTransactionManager"
)
class MysqlDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    fun mysqlDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean
    fun mysqlEntityManager(): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = mysqlDataSource()
            this.jpaVendorAdapter = HibernateJpaVendorAdapter().apply { setDatabase(Database.MYSQL) }
            this.setPackagesToScan("com.example.txeventlistenerex.entity.mysql")
        }
    }

    @Bean
    fun mysqlTransactionManager(): PlatformTransactionManager {
        return JpaTransactionManager().apply {
            this.entityManagerFactory = mysqlEntityManager().`object`
        }
    }
}