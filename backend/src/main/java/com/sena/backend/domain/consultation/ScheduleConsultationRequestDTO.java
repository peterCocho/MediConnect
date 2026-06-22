package com.sena.backend.domain.consultation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ScheduleConsultationRequestDTO {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    @NotNull
    @Future
    private OffsetDateTime consultationDate;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public OffsetDateTime getConsultationDate() {
        return consultationDate;
    }

    public void setConsultationDate(OffsetDateTime consultationDate) {
        this.consultationDate = consultationDate;
    }
}

