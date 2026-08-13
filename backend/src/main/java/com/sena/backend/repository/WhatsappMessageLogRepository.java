package com.sena.backend.repository;

import com.sena.backend.entity.WhatsappMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WhatsappMessageLogRepository extends JpaRepository<WhatsappMessageLog, Long> {

    // Fetch all unread messages for the receptionist dashboard, newest first
    List<WhatsappMessageLog> findByIsReadFalseOrderByReceivedAtDesc();
}