package com.sena.backend.service.impl;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.integration.WhatsappMessageLogDTO;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.Patient;
import com.sena.backend.entity.WhatsappMessageLog;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.PatientRepository;
import com.sena.backend.repository.WhatsappMessageLogRepository;
import com.sena.backend.service.WhatsappLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WhatsappLogServiceImpl implements WhatsappLogService {

    private final WhatsappMessageLogRepository messageLogRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WhatsappMessageLogDTO> getUnreadMessages() {
        return messageLogRepository.findByIsReadFalseOrderByReceivedAtDesc()
                .stream()
                .map(this::mapToEnrichedDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        WhatsappMessageLog log = messageLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje de WhatsApp no encontrado con id: " + id));

        log.setIsRead(true);
        messageLogRepository.save(log);
    }

    private WhatsappMessageLogDTO mapToEnrichedDTO(WhatsappMessageLog log) {
        String patientName = "Desconocido";
        String specialty = "No detectada";

        // 1. Fetch patient handling the missing '+' symbol from WhatsApp API
        String phone = log.getPhoneNumber();
        Patient patient = patientRepository.findByPhone(phone).orElse(null);

        // Fallback: If not found and doesn't start with '+', append '+' and search again
        if (patient == null && !phone.startsWith("+")) {
            patient = patientRepository.findByPhone("+" + phone).orElse(null);
        }

        if (patient != null) {
            patientName = patient.getFullName();

            // Use a final variable for the lambda expression
            final Long finalPatientId = patient.getId();

            // 2 & 3. Find ALL pending appointments for the patient and extract their unique specialties
            List<String> patientSpecialties = appointmentRepository.findByStatus(AppointmentStatus.PENDING_CONFIRMATION)
                    .stream()
                    .filter(a -> a.getPatientId().equals(finalPatientId))
                    .map(a -> doctorRepository.findById(a.getDoctorId())
                            .map(Doctor::getSpecialty)
                            .orElse("Unknown"))
                    .distinct() // Prevent duplicate specialties in the string
                    .toList();

            if (!patientSpecialties.isEmpty()) {
                specialty = String.join(", ", patientSpecialties);
            } else {
                specialty = "Sin citas pendientes";
            }
        }

        return WhatsappMessageLogDTO.builder()
                .id(log.getId())
                .phoneNumber(log.getPhoneNumber())
                .messageBody(log.getMessageBody())
                .receivedAt(log.getReceivedAt())
                .patientName(patientName)
                .specialty(specialty)
                .build();
    }
}