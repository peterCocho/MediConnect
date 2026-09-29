package com.sena.backend.service.impl;

import com.sena.backend.domain.notification.NotificationCallbackRequestDTO;
import com.sena.backend.entity.Notification;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NotificationServiceImpl.
 *
 * Cubre el callback que n8n/Evolution API invoca para reportar el estado
 * de entrega de una notificación (status + providerId).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationCallbackRequestDTO callback(Long id, String status, String providerId) {
        NotificationCallbackRequestDTO dto = new NotificationCallbackRequestDTO();
        dto.setNotificationId(id);
        dto.setStatus(status);
        dto.setProviderId(providerId);
        return dto;
    }

    @Test
    @DisplayName("updateNotificationStatus: actualiza estado, providerId y fecha de actualización, y guarda")
    void updateNotificationStatus_withExistingNotification_updatesAndSaves() throws Exception {
        OffsetDateTime oldDate = OffsetDateTime.now().minusDays(1);
        Notification notification = Notification.builder()
                .id(300L)
                .destinationNumber("573107984713")
                .status("LOGGED")
                .type("CONFIRMATION")
                .updatedAt(oldDate)
                .build();
        when(notificationRepository.findById(300L)).thenReturn(Optional.of(notification));

        notificationService.updateNotificationStatus(callback(300L, "SENT", "wamid.ABC123"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo("SENT");
        assertThat(saved.getProviderId()).isEqualTo("wamid.ABC123");
        assertThat(saved.getUpdatedAt()).isAfter(oldDate);
        // Los demás campos no se alteran
        assertThat(saved.getDestinationNumber()).isEqualTo("573107984713");
        assertThat(saved.getType()).isEqualTo("CONFIRMATION");
    }

    @Test
    @DisplayName("updateNotificationStatus: notificación inexistente lanza ResourceNotFoundException y no guarda")
    void updateNotificationStatus_withUnknownNotification_throwsResourceNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.updateNotificationStatus(callback(999L, "SENT", "x")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(notificationRepository, never()).save(any());
    }
}
