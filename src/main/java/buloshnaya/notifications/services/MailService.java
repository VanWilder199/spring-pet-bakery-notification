package buloshnaya.notifications.services;

import buloshnaya.notifications.dto.NotificationType;
import buloshnaya.notifications.dto.OrderItemDto;
import buloshnaya.notifications.dto.OrderNotification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final String fromEmail;

    public MailService(JavaMailSender mailSender,
                       @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendOrderNotification(OrderNotification order) {
        NotificationType type = order.notificationType();
        String subject = type.getSubjectPrefix() + " #" + order.orderId();
        String html = buildOrderHtml(order);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;

        try {
            helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.email());
            helper.setSubject(subject);
            helper.setText(html, true);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }


        mailSender.send(message);
    }

    private String buildOrderHtml(OrderNotification order) {
        NotificationType type = order.notificationType();

        String itemsSection = "";
        if (type != NotificationType.CANCELLED) {
            itemsSection = buildItemsSection(order);
        }

        return "<!DOCTYPE html>" +
                "<html><head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif\">" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\" style=\"padding:30px 0\">" +
                "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08)\">" +

                // Header
                "<tr><td style=\"background:" + type.getHeaderColor() + ";padding:28px 32px;text-align:center\">" +
                "<h1 style=\"margin:0;color:#fff;font-size:24px\">" + type.getHeaderTitle() + "</h1>" +
                "</td></tr>" +

                // Order info
                "<tr><td style=\"padding:24px 32px 8px\">" +
                "<p style=\"margin:0;color:#555;font-size:15px\">Заказ <strong>#" + order.orderId() + "</strong></p>" +
                "</td></tr>" +

                itemsSection +

                // Footer
                "<tr><td style=\"background:#fafafa;padding:20px 32px;text-align:center;border-top:1px solid #eee\">" +
                "<p style=\"margin:0;color:#999;font-size:13px\">Если у вас есть вопросы — просто ответьте на это письмо.</p>" +
                "</td></tr>" +

                "</table></td></tr></table></body></html>";
    }

    private String buildItemsSection(OrderNotification order) {
        StringBuilder items = new StringBuilder();
        int total = 0;

        for (OrderItemDto item : order.items()) {
            int itemTotal = item.price() * item.quantity();
            total += itemTotal;
            items.append(String.format(
                    "<tr>" +
                    "<td style=\"padding:10px 14px;border-bottom:1px solid #eee\">%s</td>" +
                    "<td style=\"padding:10px 14px;border-bottom:1px solid #eee;text-align:center\">%d</td>" +
                    "<td style=\"padding:10px 14px;border-bottom:1px solid #eee;text-align:right\">%d ₽</td>" +
                    "<td style=\"padding:10px 14px;border-bottom:1px solid #eee;text-align:right\">%d ₽</td>" +
                    "</tr>",
                    item.productName(), item.quantity(), item.price(), itemTotal));
        }

        return // Items table
                "<tr><td style=\"padding:16px 32px\">" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-size:14px;color:#333\">" +
                "<tr style=\"background:#fafafa\">" +
                "<th style=\"padding:10px 14px;text-align:left;border-bottom:2px solid #d4a373\">Товар</th>" +
                "<th style=\"padding:10px 14px;text-align:center;border-bottom:2px solid #d4a373\">Кол-во</th>" +
                "<th style=\"padding:10px 14px;text-align:right;border-bottom:2px solid #d4a373\">Цена</th>" +
                "<th style=\"padding:10px 14px;text-align:right;border-bottom:2px solid #d4a373\">Сумма</th>" +
                "</tr>" +
                items +
                "</table>" +
                "</td></tr>" +

                // Total
                "<tr><td style=\"padding:8px 32px 24px;text-align:right\">" +
                "<p style=\"margin:0;font-size:18px;color:#333\"><strong>Итого: " + total + " ₽</strong></p>" +
                "</td></tr>";
    }
}
