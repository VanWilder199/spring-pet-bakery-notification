package buloshnaya.notifications.repository;

import buloshnaya.notifications.entity.NotificationOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutboxEntity, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE NotificationOutboxEntity e SET e.status = :status, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void updateStatus(Long id, String status);
}
