package com.sena.backend.service;

import com.sena.backend.domain.notification.NotificationCallbackRequestDTO;

public interface NotificationService {
    void updateNotificationStatus(NotificationCallbackRequestDTO dto) throws Exception;
}
