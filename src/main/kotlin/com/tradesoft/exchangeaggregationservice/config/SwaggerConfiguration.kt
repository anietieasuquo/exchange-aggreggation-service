package com.tradesoft.exchangeaggregationservice.config

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE


@Configuration
class SwaggerConfiguration(private val appProperties: AppProperties) {
    private val mediaType: Set<String> = setOf(APPLICATION_JSON_VALUE)

    @Bean
    fun aggregationAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .contact(contact())
                    .title(appProperties.title)
                    .description(appProperties.description)
                    .version(appProperties.version)
                    .license(
                        License()
                            .name(appProperties.license)
                            .url(appProperties.licenseUrl)
                    )
                    .termsOfService(appProperties.tosUrl)
                    .summary(appProperties.description)
            )
            .externalDocs(
                ExternalDocumentation()
                    .description("SpringShop Wiki Documentation")
                    .url("https://springshop.wiki.github.org/docs")
            )
    }

    @Bean
    fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("TradeSoft")
            .packagesToScan("com.tradesoft.exchangeaggregationservice.periphery.controller")
            .pathsToMatch("/api/**")
            .displayName(appProperties.title)
            .producesToMatch(mediaType.toString())
            .consumesToMatch(mediaType.toString())
            .build()
    }

    private fun contact(): Contact = Contact()
        .email(appProperties.authorEmail)
        .url(appProperties.authorUrl)
        .name(appProperties.authorName)
}
