package com.tradesoft.exchangeaggregationservice.config.events

import com.tradesoft.exchangeaggregationservice.config.async.AsyncProvider
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConfig(
    private val kafkaTopic: KafkaTopic,
    private val kafkaProperties: KafkaProperties,
    private val asyncProvider: AsyncProvider
) {

    @Bean
    fun consumerFactory(): ConsumerFactory<String?, Any?> {
        val consumerProperties: MutableMap<String, Any> = HashMap()
        consumerProperties[BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers
        consumerProperties[GROUP_ID_CONFIG] = kafkaProperties.consumer.groupId
        consumerProperties[KEY_DESERIALIZER_CLASS_CONFIG] = kafkaProperties.consumer.keyDeserializer
        consumerProperties[VALUE_DESERIALIZER_CLASS_CONFIG] = kafkaProperties.consumer.valueDeserializer
        consumerProperties[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = StringDeserializer::class.java
        consumerProperties[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JsonDeserializer::class.java
        consumerProperties[JsonDeserializer.TRUSTED_PACKAGES] =
            "com.tradesoft.exchangeaggregationservice.periphery.events"
        consumerProperties[AUTO_OFFSET_RESET_CONFIG] = kafkaProperties.consumer.autoOffsetReset
        return DefaultKafkaConsumerFactory(consumerProperties)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory()
        factory.setConcurrency(kafkaProperties.listener.concurrency)
        factory.containerProperties.ackMode = kafkaProperties.listener.ackMode
        factory.containerProperties.isSyncCommits = true
        factory.containerProperties.idleBetweenPolls = kafkaProperties.listener.idleBetweenPolls.seconds
        factory.containerProperties.pollTimeout = kafkaProperties.listener.pollTimeout.seconds
        factory.containerProperties.listenerTaskExecutor = asyncProvider.provideAsyncTaskExecutor()
        return factory
    }

    @Bean
    fun exchangeMetaDataTopic(): NewTopic = NewTopic(kafkaTopic.exchangeMetadata, 1, 1.toShort())
}
