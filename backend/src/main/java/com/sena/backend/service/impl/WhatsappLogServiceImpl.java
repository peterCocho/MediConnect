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
import java.util.Map;
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

            // 2. Fetch only this patient's PENDING_CONFIRMATION appointments (filtered in the DB,
            List<Appointment> pendingAppointments = appointmentRepository
                    .findByPatientIdAndStatus(patient.getId(), AppointmentStatus.PENDING_CONFIRMATION);

            if (!pendingAppointments.isEmpty()) {
                // 3. Fetch the doctors involved in a single query instead of a
                // findById per appointment.
                List<Long> doctorIds = pendingAppointments.stream()
                        .map(Appointment::getDoctorId)
                        .distinct()
                        .toList();

                Map<Long, String> specialtyByDoctorId = doctorRepository.findAllById(doctorIds).stream()
                        .collect(Collectors.toMap(Doctor::getId, Doctor::getSpecialty));

                List<String> patientSpecialties = doctorIds.stream()
                        .map(doctorId -> specialtyByDoctorId.getOrDefault(doctorId, "Unknown"))
                        .distinct() // Prevent duplicate specialties in the string
                        .toList();

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