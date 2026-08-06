package com.sena.backend.service.impl;

import com.sena.backend.ConsultationScheduledEvent;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.Patient;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.event.AppointmentCanceledEvent;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.MedicalRecordRepository;
import com.sena.backend.repository.PatientRepository;
import com.sena.backend.service.AppointmentService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    // Dependencies required to link the clinical domain
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
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con id: " + patientId));

        if (!patient.getIsActive()) {
            throw new BusinessRuleException("No se puede agendar: El paciente se encuentra inactivo.");
        }

        // 1. Acquire pessimistic lock on the doctor's record to serialize concurrent requests
        Doctor doctor = doctorRepository.findByIdWithLock(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con id: " + doctorId));

        if (doctor.getUser() != null && !doctor.getUser().getIsActive()) {
            throw new BusinessRuleException("No se puede agendar: El médico se encuentra inactivo en el sistema.");
        }

        // 2. Check for time overlaps. Safe from race conditions due to the lock above.
        if (appointmentRepository.hasOverlappingAppointments(doctorId, start, end)) {
            throw new BusinessRuleException("El médico seleccionado ya tiene una cita que se cruza con este horario.");
        }

        MedicalRecord mr = medicalRecordRepository.findByPatientAndDoctor(patient, doctor)
                .orElseGet(() -> {
                    MedicalRecord newRecord = new MedicalRecord();
                    newRecord.setPatient(patient);
                    newRecord.setDoctor(doctor);
                    newRecord.setRecordNumber("MR-" + patient.getId() + "-" + doctor.getId());
                    newRecord.setDiagnosis("Pending");
                    return medicalRecordRepository.save(newRecord);
                });

        // 3. Save without try-catch loops. The DB lock guarantees consistency.
        Appointment ap = new Appointment();
        ap.setPatientId(patientId);
        ap.setDoctorId(doctorId);
        ap.setStartTime(start);
        ap.setEndTime(end);
        ap.setStatus("BOOKED");
        Appointment savedAppointment = appointmentRepository.save(ap);

        Consultation c = new Consultation();
        c.setConsultationDate(start);
        c.setStatus("SCHEDULED");
        c.setDoctor(doctor);
        c.setMedicalRecord(mr);
        c.setAppointment(savedAppointment);

        Consultation savedConsultation = consultationRepository.save(c);
        eventPublisher.publishEvent(new ConsultationScheduledEvent(this, savedConsultation.getId()));

        return savedAppointment;
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + appointmentId));

        if ("CANCELED".equals(appointment.getStatus())) {
            throw new BusinessRuleException("La cita ya se encuentra cancelada.");
        }
        if ("COMPLETED".equals(appointment.getStatus())) {
            throw new BusinessRuleException("No se puede cancelar una cita que ya fue completada.");
        }

        appointment.setStatus("CANCELED");
        appointment.setCancellationReason(reason);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Propagate cancellation to the clinical domain
        consultationRepository.findByAppointmentId(appointmentId)
                .ifPresent(consultation -> {
                    consultation.setStatus("CANCELED");
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
        OffsetDateTime start = (startDate != null) ? startDate : OffsetDateTime.now();
        OffsetDateTime end = (endDate != null) ? endDate : start.plusYears(1);

        if (start.isAfter(end)) {
            throw new BusinessRuleException("La fecha de inicio no puede ser posterior a la fecha de finalización.");
        }

        return appointmentRepository.findByStartTimeBetween(start, end, pageable);
    }
}