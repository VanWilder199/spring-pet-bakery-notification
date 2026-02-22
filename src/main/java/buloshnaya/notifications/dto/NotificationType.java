package buloshnaya.notifications.dto;

import lombok.Getter;

@Getter
public enum NotificationType {
    CONFIRMED("Подтверждение заказа", "#d4a373", "Спасибо за заказ!"),
    PAID("Оплата получена", "#6b9e5b", "Оплата прошла успешно!"),
    DELIVERING("Заказ в пути", "#5b8fb9", "Ваш заказ уже в пути!"),
    DELIVERED("Заказ доставлен", "#6b9e5b", "Заказ доставлен!"),
    CANCELLED("Заказ отменён", "#c0392b", "Заказ отменён");

    private final String subjectPrefix;
    private final String headerColor;
    private final String headerTitle;

    NotificationType(String subjectPrefix, String headerColor, String headerTitle) {
        this.subjectPrefix = subjectPrefix;
        this.headerColor = headerColor;
        this.headerTitle = headerTitle;
    }
}
