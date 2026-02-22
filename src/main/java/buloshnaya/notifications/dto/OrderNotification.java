package buloshnaya.notifications.dto;

import java.util.List;

public record OrderNotification(
    String orderId,
    String userId,
    String email,
    NotificationType notificationType,
    List<OrderItemDto> items
    ) {}
