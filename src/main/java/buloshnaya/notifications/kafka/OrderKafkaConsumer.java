package buloshnaya.notifications.kafka;

import buloshnaya.notifications.dto.OrderNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderKafkaConsumer.class);


    @KafkaListener(topics = "order-notification-topic", groupId = "notification-group")
    public void consumeOrderNotification(OrderNotification orderNotification) {

        logger.info("Received order notification: {}", orderNotification.toString());
    }
}
