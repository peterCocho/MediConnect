package com.sena.backend.service;

import com.sena.backend.domain.consultation.ScheduleConsultationRequestDTO;
import com.sena.backend.entity.Consultation;

public interface ConsultationService {
    Consultation scheduleConsultation(ScheduleConsultationRequestDTO dto) throws Exception;
}
