package buloshnaya.notifications.config;

import buloshnaya.notifications.dto.OrderNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderNotification> producerFactory(
            ObjectMapper objectMapper
    )  {
        Map<String, Object> configProperties = new HashMap<>();
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");

        JsonDeserializer<OrderNotification> jsonDeserializer = new JsonDeserializer<>(objectMapper) {
        };

        return new DefaultKafkaConsumerFactory<>(configProperties, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderNotification> kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderNotification> consumerFactory
    ) {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String,OrderNotification>();
        containerFactory.setConcurrency(1);
        containerFactory.setConsumerFactory(consumerFactory);
        return containerFactory;
    }
}