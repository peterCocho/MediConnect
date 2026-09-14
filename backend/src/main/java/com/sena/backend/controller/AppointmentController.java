package com.sena.backend.controller;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.appointment.BookAppointmentRequestDTO;
import com.sena.backend.domain.appointment.CancelAppointmentRequestDTO;
import com.sena.backend.domain.appointment.AppointmentResponseDTO;
import com.sena.backend.entity.Appointment;
import com.sena.backend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<AppointmentResponseDTO> bookAppointment(@Valid @RequestBody BookAppointmentRequestDTO request) throws Exception {
        Appointment appointment = appointmentService.bookAppointment(
                request.getPatientId(),
                request.getDoctorId(),
                request.getStartTime(),
                request.getEndTime()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDTO(appointment));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CancelAppointmentRequestDTO request
    ) {
        Appointment cancelledAppointment = appointmentService.cancelAppointment(id, request.reason());
        return ResponseEntity.ok(convertToResponseDTO(cancelledAppointment));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<AppointmentResponseDTO> getAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(convertToResponseDTO(appointment));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAllAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Appointment> appointments = appointmentService.getAllAppointments(startDate, endDate, pageable);
        // Map page elements from Entity to Response DTO
        Page<AppointmentResponseDTO> response = appointments.map(this::convertToResponseDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getPatientAppointments(
            @PathVariable Long patientId,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Appointment> appointments = appointmentService.getPatientAppointments(patientId, pageable);
        // Map paginated historical data for a specific patient
        Page<AppointmentResponseDTO> response = appointments.map(this::convertToResponseDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Appointment> appointments = appointmentService.getDoctorAppointments(doctorId, pageable);
        // Map paginated agenda data for a specific doctor
        Page<AppointmentResponseDTO> response = appointments.map(this::convertToResponseDTO);
        return ResponseEntity.ok(response);
    }

    // Nuevo endpoint para las citas "Por Revisar" (Naranja)
    @GetMapping("/pending-confirmation")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getPendingConfirmations() {
        // Asumiendo que agregas un findByStatus en AppointmentRepository
        // y su lógica correspondiente en AppointmentService
        List<Appointment> pending = appointmentService.getAppointmentsByStatus(AppointmentStatus.PENDING_CONFIRMATION);
        List<AppointmentResponseDTO> response = pending.stream().map(this::convertToResponseDTO).toList();
        return ResponseEntity.ok(response);
    }

    // Helper method to uncouple the database entity from the web response layer
    private AppointmentResponseDTO convertToResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .consultationId(appointment.getConsultation() != null ? appointment.getConsultation().getId() : null)
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .cancellationReason(appointment.getCancellationReason())
                .build();
    }
}