package com.sena.backend.service;

import com.sena.backend.domain.integration.WhatsappMessageLogDTO;
import java.util.List;

public interface WhatsappLogService {

    // Retrieves all unread messages sorted by newest first
    List<WhatsappMessageLogDTO> getUnreadMessages();

    // Marks a specific message as read by the receptionist
    void markAsRead(Long id);
}