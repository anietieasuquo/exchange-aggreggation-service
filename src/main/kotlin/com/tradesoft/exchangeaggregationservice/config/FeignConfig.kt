package com.tradesoft.exchangeaggregationservice.config

import feign.Client
import feign.Feign
import feign.Logger.Level.BASIC
import feign.RequestInterceptor
import feign.codec.Decoder
import feign.codec.Encoder
import org.springframework.cloud.openfeign.FeignClientsConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(FeignClientsConfiguration::class)
class FeignConfig(
    val interceptors: List<RequestInterceptor>,
    val encoder: Encoder,
    val decoder: Decoder
) {

    @Bean
    fun authInterceptorFeignBuilder(): Feign.Builder = Feign.builder()
        .encoder(encoder)
        .decoder(decoder)
        .logLevel(BASIC)
        .requestInterceptors(interceptors)
        .client(Client.Default(null, null))
}
