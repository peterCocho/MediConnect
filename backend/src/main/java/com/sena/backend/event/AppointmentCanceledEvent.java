package com.sena.backend.event;

import org.springframework.context.ApplicationEvent;

public class AppointmentCanceledEvent extends ApplicationEvent {
    private final Long appointmentId;

    public AppointmentCanceledEvent(Object source, Long appointmentId) {
        super(source);
        this.appointmentId = appointmentId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }
}