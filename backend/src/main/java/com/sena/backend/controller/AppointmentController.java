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
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.mapToDTO(appointment));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CancelAppointmentRequestDTO request
    ) {
        Appointment cancelledAppointment = appointmentService.cancelAppointment(id, request.reason());
        return ResponseEntity.ok(appointmentService.mapToDTO(cancelledAppointment));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<AppointmentResponseDTO> getAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointmentService.mapToDTO(appointment));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAllAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Appointment> appointments = appointmentService.getAllAppointments(startDate, endDate, pageable);
        return ResponseEntity.ok(appointments.map(appointmentService::mapToDTO));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getPatientAppointments(
            @PathVariable Long patientId,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Appointment> appointments = appointmentService.getPatientAppointments(patientId, pageable);
        return ResponseEntity.ok(appointments.map(appointmentService::mapToDTO));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Appointment> appointments = appointmentService.getDoctorAppointments(doctorId, pageable);
        return ResponseEntity.ok(appointments.map(appointmentService::mapToDTO));
    }

    @GetMapping("/pending-confirmation")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getPendingConfirmations() {
        List<Appointment> pending = appointmentService.getAppointmentsByStatus(AppointmentStatus.PENDING_CONFIRMATION);
        List<AppointmentResponseDTO> response = pending.stream().map(appointmentService::mapToDTO).toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(@PathVariable Long id) {
        Appointment confirmedAppointment = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(appointmentService.mapToDTO(confirmedAppointment));
    }
}