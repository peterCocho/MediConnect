package com.sena.backend.event;

import org.springframework.context.ApplicationEvent;

public class AppointmentRequiresConfirmationEvent extends ApplicationEvent {

    private final Long appointmentId;

    public AppointmentRequiresConfirmationEvent(Object source, Long appointmentId) {
        super(source);
        this.appointmentId = appointmentId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }
}