package com.sena.backend.service;

import com.sena.backend.domain.NotificationCallbackRequestDTO;

public interface NotificationService {
    void updateNotificationStatus(NotificationCallbackRequestDTO dto) throws Exception;
}
