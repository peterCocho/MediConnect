package com.sena.backend.domain.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationCallbackRequestDTO {

    @NotNull(message = "Notification ID is mandatory")
    @JsonProperty("notification_id")
    private Long notificationId;

    @NotBlank(message = "Status is mandatory")
    @JsonProperty("status")
    private String status;

    @JsonProperty("provider_id")
    private String providerId;

}