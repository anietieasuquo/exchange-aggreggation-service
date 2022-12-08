package com.tradesoft.exchangeaggregationservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement


@SpringBootApplication
@EnableConfigurationProperties
@EnableTransactionManagement
@EnableJpaRepositories
class ExchangeAggregationServiceApplication

fun main(args: Array<String>) {
    runApplication<ExchangeAggregationServiceApplication>(*args)
}
