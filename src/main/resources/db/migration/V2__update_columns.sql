ALTER TABLE notification_outbox
    ADD COLUMN notification_type VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE notification_outbox
    DROP CONSTRAINT notification_outbox_order_id_key;

 ALTER TABLE notification_outbox
   ADD CONSTRAINT uq_order_notification_type UNIQUE (order_id, notification_type);


 ALTER TABLE notification_outbox
     ADD CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'));
