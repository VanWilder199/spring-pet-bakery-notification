package buloshnaya.notifications.config;

import buloshnaya.notifications.dto.OrderNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import jakarta.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Bean
    public ConsumerFactory<String, OrderNotification> consumerFactory(
            ObjectMapper objectMapper
    ) {
        Map<String, Object> configProperties = new HashMap<>();
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");

        JsonDeserializer<OrderNotification> jsonDeserializer = new JsonDeserializer<>(objectMapper) {
        };

        return new DefaultKafkaConsumerFactory<>(configProperties, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ProducerFactory<String, OrderNotification> producerFactory(
            ObjectMapper objectMapper
    ) {
        Map<String, Object> configProperties = new HashMap<>();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        return new DefaultKafkaProducerFactory<>(
                configProperties,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
    }

    @Bean
    public KafkaTemplate<String, OrderNotification> kafkaTemplate(
            ProducerFactory<String, OrderNotification> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, OrderNotification> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));
        handler.addNotRetryableExceptions(DeserializationException.class, ValidationException.class);
        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderNotification> kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderNotification> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, OrderNotification>();
        containerFactory.setConcurrency(4);
        containerFactory.setConsumerFactory(consumerFactory);
        containerFactory.setCommonErrorHandler(errorHandler);
        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return containerFactory;
    }
}