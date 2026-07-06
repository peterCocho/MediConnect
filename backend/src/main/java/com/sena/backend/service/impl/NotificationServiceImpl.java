package com.sena.backend.service.impl;

import com.sena.backend.domain.NotificationCallbackRequestDTO;
import com.sena.backend.entity.Notification;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.NotificationRepository;
import com.sena.backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void updateNotificationStatus(NotificationCallbackRequestDTO dto) throws Exception {
        Notification n = notificationRepository.findById(dto.getNotification_id())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + dto.getNotification_id()));

        n.setStatus(dto.getStatus());
        n.setProviderId(dto.getProvider_id());
        n.setUpdatedAt(OffsetDateTime.now());

        notificationRepository.save(n);
    }
}
