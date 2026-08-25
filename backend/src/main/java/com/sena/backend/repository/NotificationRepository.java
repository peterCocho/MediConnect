package com.sena.backend.repository;

import com.sena.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Checks if a specific notification type was already triggered for a consultation
    boolean existsByConsultationIdAndType(Long consultationId, String type);
}
