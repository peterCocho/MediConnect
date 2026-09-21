package com.sena.backend.service.impl;

import com.sena.backend.ConsultationScheduledEvent;
import com.sena.backend.domain.appointment.AppointmentResponseDTO;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.Patient;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.event.AppointmentCanceledEvent;
import com.sena.backend.event.AppointmentRequiresConfirmationEvent;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.MedicalRecordRepository;
import com.sena.backend.repository.PatientRepository;
import com.sena.backend.service.AppointmentService;
import com.sena.backend.domain.AppointmentStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ConsultationRepository consultationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  PatientRepository patientRepository,
                                  DoctorRepository doctorRepository,
                                  MedicalRecordRepository medicalRecordRepository,
                                  ConsultationRepository consultationRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.consultationRepository = consultationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Appointment bookAppointment(Long patientId, Long doctorId, OffsetDateTime start, OffsetDateTime end) {

        java.time.ZoneId clinicZone = java.time.ZoneId.of("America/Bogota");
        java.time.ZonedDateTime startBogota = start.atZoneSameInstant(clinicZone);
        java.time.ZonedDateTime endBogota = end.atZoneSameInstant(clinicZone);

        // 2. Validar que no sea domingo (según el calendario de la clínica)
        if (startBogota.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            throw new BusinessRuleException("No se puede agendar: La clínica no ofrece atención los días domingo.");
        }

        // 3. Validar horario de atención estricto (08:00 AM a 6:00 PM en Colombia)
        java.time.LocalTime startTime = startBogota.toLocalTime();
        java.time.LocalTime endTime = endBogota.toLocalTime();

        java.time.LocalTime openingTime = java.time.LocalTime.of(8, 0);  // 08:00 AM
        java.time.LocalTime closingTime = java.time.LocalTime.of(18, 0); // 18:00 PM (6:00 PM)

        if (startTime.isBefore(openingTime) || endTime.isAfter(closingTime)) {
            throw new BusinessRuleException("No se puede agendar: El horario está fuera de la jornada de atención (8:00 AM a 6:00 PM).");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con id: " + patientId));

        if (!patient.getIsActive()) {
            throw new BusinessRuleException("No se puede agendar: El paciente se encuentra inactivo.");
        }

        // 1. Check for patient time overlaps before acquiring doctor locks
        if (appointmentRepository.hasPatientOverlappingAppointments(patientId, start, end)) {
            throw new BusinessRuleException("El paciente ya tiene una cita programada que se cruza con este horario.");
        }

        // 1. Acquire pessimistic lock on the doctor's record to serialize concurrent requests
        Doctor doctor = doctorRepository.findByIdWithLock(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con id: " + doctorId));

        if (doctor.getUser() != null && !doctor.getUser().getIsActive()) {
            throw new BusinessRuleException("No se puede agendar: El médico se encuentra inactivo en el sistema.");
        }

        // 2. Check for time overlaps. Safe from race conditions due to patient/doctor locks.
        if (appointmentRepository.hasOverlappingAppointments(doctorId, start, end)) {
            throw new BusinessRuleException("El médico seleccionado ya tiene una cita que se cruza con este horario.");
        }

        // Retrieve the patient's unique medical record (1:1 relationship)
        MedicalRecord mr = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente médico no encontrado para el paciente con id: " + patientId));

        // 3. Save appointment using the new Enum and initial state
        Appointment ap = new Appointment();
        ap.setPatientId(patientId);
        ap.setDoctorId(doctorId);
        ap.setStartTime(start);
        ap.setEndTime(end);
        ap.setStatus(AppointmentStatus.PENDING_CONFIRMATION);
        Appointment savedAppointment = appointmentRepository.save(ap);

        // 4. Create and link consultation to the unique medical record using synchronized state
        Consultation c = new Consultation();
        c.setConsultationDate(start);
        c.setStatus(AppointmentStatus.PENDING_CONFIRMATION);
        c.setDoctor(doctor);
        c.setMedicalRecord(mr);
        c.setAppointment(savedAppointment);

        Consultation savedConsultation = consultationRepository.save(c);
        eventPublisher.publishEvent(new ConsultationScheduledEvent(this, savedConsultation.getId()));

        eventPublisher.publishEvent(new AppointmentRequiresConfirmationEvent(this, savedAppointment.getId()));
        return savedAppointment;
    }

    @Override
    @Transactional
    public Appointment confirmAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + appointmentId));

        // Idempotency check: if it is already scheduled, return it successfully without throwing an error
        if (appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            return appointment;
        }

        // Strictly validate that only pending appointments can be confirmed
        if (appointment.getStatus() != AppointmentStatus.PENDING_CONFIRMATION) {
            throw new BusinessRuleException("La cita no puede ser confirmada porque su estado actual es: " + appointment.getStatus().name());
        }

        // Apply transition to SCHEDULED for the appointment
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Synchronize the transition with the clinical domain
        consultationRepository.findByAppointmentId(appointmentId)
                .ifPresent(consultation -> {
                    consultation.setStatus(AppointmentStatus.SCHEDULED);
                    consultationRepository.save(consultation);
                });

        return savedAppointment;
    }


    @Override
    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + appointmentId));

        // Validate state transitions using the Enum
        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new BusinessRuleException("La cita ya se encuentra cancelada.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("No se puede cancelar una cita que ya fue completada.");
        }

        // Apply cancellation state
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setCancellationReason(reason);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Propagate cancellation to the clinical domain
        consultationRepository.findByAppointmentId(appointmentId)
                .ifPresent(consultation -> {
                    consultation.setStatus(AppointmentStatus.CANCELED);
                    consultationRepository.save(consultation);
                });

        eventPublisher.publishEvent(new AppointmentCanceledEvent(this, appointmentId));

        return savedAppointment;
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appointment> getPatientAppointments(Long patientId, Pageable pageable) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Paciente no encontrado con id: " + patientId);
        }
        return appointmentRepository.findByPatientId(patientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appointment> getDoctorAppointments(Long doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Médico no encontrado con id: " + doctorId);
        }
        return appointmentRepository.findByDoctorId(doctorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appointment> getAllAppointments(OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable) {
        if (startDate == null && endDate == null) {
            return appointmentRepository.findAll(pageable);
        }

        OffsetDateTime start = (startDate != null) ? startDate : OffsetDateTime.MIN;
        OffsetDateTime end = (endDate != null) ? endDate : OffsetDateTime.MAX;

        if (start.isAfter(end)) {
            throw new BusinessRuleException("La fecha de inicio no puede ser posterior a la fecha de finalización.");
        }

        return appointmentRepository.findByStartTimeBetween(start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getPendingAppointmentsEnriched() {
        List<Appointment> pendingAppointments = appointmentRepository.findByStatus(AppointmentStatus.PENDING_CONFIRMATION);
        return pendingAppointments.stream()
                .map(this::convertToEnrichedDTO)
                .toList();
    }

    private AppointmentResponseDTO convertToEnrichedDTO(Appointment appointment) {
        // Extraer nombre del paciente
        String patientName = patientRepository.findById(appointment.getPatientId())
                .map(Patient::getFullName)
                .orElse("Paciente Desconocido");

        // Extraer datos del doctor
        String doctorName = "No asignado";
        String specialty = "Sin especialidad";

        Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
        if (doctor != null) {
            doctorName = doctor.getFullName();
            specialty = doctor.getSpecialty();
        }

        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .cancellationReason(appointment.getCancellationReason())
                .patientName(patientName)
                .doctorName(doctorName)
                .specialty(specialty)
                .build();
    }

    @Override
    public AppointmentResponseDTO mapToDTO(Appointment appointment) {
        String patientName = patientRepository.findById(appointment.getPatientId())
                .map(Patient::getFullName)
                .orElse("Paciente Desconocido");

        String doctorName = "No asignado";
        String specialty = "Sin especialidad";

        Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
        if (doctor != null) {
            doctorName = doctor.getFullName();
            specialty = doctor.getSpecialty();
        }

        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .consultationId(appointment.getConsultation() != null ? appointment.getConsultation().getId() : null)
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .cancellationReason(appointment.getCancellationReason())
                .patientName(patientName)
                .doctorName(doctorName)
                .specialty(specialty)
                .build();
    }
}