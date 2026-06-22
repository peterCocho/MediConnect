package com.sena.backend;

import org.springframework.context.ApplicationEvent;

public class ConsultationScheduledEvent extends ApplicationEvent {
    private final Long consultationId;

    public ConsultationScheduledEvent(Object source, Long consultationId) {
        super(source);
        this.consultationId = consultationId;
    }

    public Long getConsultationId() { return consultationId; }
}
