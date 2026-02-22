package buloshnaya.notifications.kafka;

import buloshnaya.notifications.dto.OrderNotification;
import buloshnaya.notifications.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderKafkaConsumer.class);

    private final MailService mailService;

    public OrderKafkaConsumer(MailService mailService) {
        this.mailService = mailService;
    }

    @KafkaListener(topics = "order-notification-topic", groupId = "notification-group")
    public void consumeOrderNotification(OrderNotification orderNotification) {
        logger.info("Received  {} ordrNotification  {}", orderNotification.notificationType(), orderNotification.orderId());

        try {
            mailService.sendOrderNotification(orderNotification);
            logger.info("Sent {} email for order {}", orderNotification.notificationType(), orderNotification.orderId());
        } catch (Exception e) {
            logger.error("Failed to send email for order {}: {}", orderNotification.orderId(), e.getMessage(), e);
        }
    }
}
