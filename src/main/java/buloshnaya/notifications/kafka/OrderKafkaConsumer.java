package buloshnaya.notifications.kafka;

import buloshnaya.notifications.dto.OrderNotification;
import buloshnaya.notifications.entity.NotificationOutboxEntity;
import buloshnaya.notifications.repository.NotificationOutboxRepository;
import buloshnaya.notifications.services.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderKafkaConsumer.class);

    private final MailService mailService;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final ObjectMapper objectMapper;

    public OrderKafkaConsumer(MailService mailService,
                               NotificationOutboxRepository notificationOutboxRepository,
                               ObjectMapper objectMapper) {
        this.mailService = mailService;
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-notification-topic", groupId = "notification-group")
    public void consumeOrderNotification(ConsumerRecord<String, String> record,
                                         Acknowledgment acknowledgment) {
        OrderNotification notification;
        try {
            notification = objectMapper.readValue(record.value(), OrderNotification.class);
        } catch (Exception e) {
            logger.error("Failed to deserialize notification: {}", e.getMessage());
            acknowledgment.acknowledge();
            return;
        }

        String orderId = notification.orderId();
        String type = notification.notificationType().name();

        logger.info("Received {} for order {}", type, orderId);

        NotificationOutboxEntity entity = buildOutboxEntity(record, notification);

        try {
            notificationOutboxRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            logger.warn("Duplicate notification skipped: order={}, type={}", orderId, type);
            acknowledgment.acknowledge();
            return;
        }

        try {
            mailService.sendOrderNotification(notification);
            notificationOutboxRepository.updateStatus(entity.getId(), "SENT");
            logger.info("Sent {} email for order {}", type, orderId);
        } catch (Exception e) {
            notificationOutboxRepository.updateStatus(entity.getId(), "FAILED");
            logger.error("Failed to send {} email for order {}: {}", type, orderId, e.getMessage());
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private NotificationOutboxEntity buildOutboxEntity(ConsumerRecord<String, String> record,
                                                       OrderNotification notification) {
        NotificationOutboxEntity entity = new NotificationOutboxEntity();
        entity.setOrderId(notification.orderId());
        entity.setNotificationType(notification.notificationType());
        entity.setStatus("PENDING");
        entity.setKafkaTopic(record.topic());
        entity.setKafkaPartition(record.partition());
        entity.setKafkaOffset(record.offset());

        return entity;
    }
}
