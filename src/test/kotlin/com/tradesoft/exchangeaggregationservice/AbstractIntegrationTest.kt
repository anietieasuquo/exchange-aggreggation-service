package com.tradesoft.exchangeaggregationservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType.BLOCKCHAIN_DOT_COM
import com.tradesoft.exchangeaggregationservice.core.business.enums.MetadataUploadStatus.PENDING
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataEntity
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataUploadEntity
import com.tradesoft.exchangeaggregationservice.core.repository.ExchangeMetadataEntityRepository
import com.tradesoft.exchangeaggregationservice.core.repository.ExchangeMetadataUploadEntityRepository
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File

@SpringBootTest(classes = [ExchangeAggregationServiceApplication::class])
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 3000)
@DirtiesContext
@Testcontainers
abstract class AbstractIntegrationTest {

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var exchangeMetadataEntityRepository: ExchangeMetadataEntityRepository

    @Autowired
    protected lateinit var exchangeMetadataUploadEntityRepository: ExchangeMetadataUploadEntityRepository

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:12").apply {
            withDatabaseName("exchange-aggregation-service-test")
            withUsername("test")
            withPassword("test")
            withAccessToHost(true)
        }

        @Container
        val redisContainer = GenericContainer<Nothing>(DockerImageName.parse("redis:latest")).apply {
            withExposedPorts(6379)
            withAccessToHost(true)
        }

        @Container
        val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            redisContainer.start()
            postgresContainer.start()
            kafkaContainer.start()

            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.datasource.username", postgresContainer::getUsername)

            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort)

            registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers)
        }
    }

    @BeforeEach
    fun setUp() {
        assertThat(postgresContainer.isRunning).isTrue
        assertThat(redisContainer.isRunning).isTrue
        assertThat(kafkaContainer.isRunning).isTrue
    }

    protected fun initExchangeMetadataEntity(): ExchangeMetadataEntity =
        exchangeMetadataEntityRepository.save(
            ExchangeMetadataEntity(
                version = 1,
                exchangeType = BLOCKCHAIN_DOT_COM,
                dataKey = "Country",
                dataValue = "UK",
                uploadId = 10
            )
        )

    protected fun initExchangeMetadataUploadEntity(): ExchangeMetadataUploadEntity =
        exchangeMetadataUploadEntityRepository.save(
            ExchangeMetadataUploadEntity(
                version = 1,
                exchangeType = BLOCKCHAIN_DOT_COM,
                status = PENDING,
                file = null
            )
        )

    protected fun getTestMetadataUploadFile(fileName: String): ByteArray =
        File(this.javaClass.classLoader.getResource(fileName)?.file ?: throw RuntimeException("Test file not found"))
            .let { FileUtils.readFileToByteArray(it) }
}
