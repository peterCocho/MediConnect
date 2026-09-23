package com.sena.backend.controller;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.appointment.BookAppointmentRequestDTO;
import com.sena.backend.domain.appointment.CancelAppointmentRequestDTO;
import com.sena.backend.domain.appointment.AppointmentResponseDTO;
import com.sena.backend.entity.Appointment;
import com.sena.backend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Appointments", description = "Endpoints for managing medical appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Operation(summary = "Book a new appointment", description = "Books a new medical appointment. Requires RECEPTION role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or scheduling conflict"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
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

    @Operation(summary = "Cancel an appointment", description = "Cancels an existing appointment with a provided reason. Requires RECEPTION role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment successfully cancelled"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or appointment cannot be cancelled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CancelAppointmentRequestDTO request
    ) {
        Appointment cancelledAppointment = appointmentService.cancelAppointment(id, request.reason());
        return ResponseEntity.ok(appointmentService.mapToDTO(cancelledAppointment));
    }

    @Operation(summary = "Get appointment by ID", description = "Retrieves the details of a specific appointment. Requires RECEPTION role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<AppointmentResponseDTO> getAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointmentService.mapToDTO(appointment));
    }

    @Operation(summary = "Get all appointments", description = "Retrieves a paginated list of all appointments, optionally filtered by date range. Accessible by ADMIN and RECEPTIONIST roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of appointments successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid date format parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
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

    @Operation(summary = "Get patient appointments", description = "Retrieves a paginated list of appointments for a specific patient. Requires RECEPTION role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient appointments successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getPatientAppointments(
            @PathVariable Long patientId,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Appointment> appointments = appointmentService.getPatientAppointments(patientId, pageable);
        return ResponseEntity.ok(appointments.map(appointmentService::mapToDTO));
    }

    @Operation(summary = "Get doctor appointments", description = "Retrieves a paginated list of appointments for a specific doctor. Accessible by ADMIN and RECEPTIONIST roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor appointments successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Appointment> appointments = appointmentService.getDoctorAppointments(doctorId, pageable);
        return ResponseEntity.ok(appointments.map(appointmentService::mapToDTO));
    }

    @Operation(summary = "Get pending confirmations", description = "Retrieves a list of all appointments currently pending confirmation. Accessible by ADMIN and RECEPTIONIST roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending appointments successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping("/pending-confirmation")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<List<AppointmentResponseDTO>> getPendingConfirmations() {
        List<Appointment> pending = appointmentService.getAppointmentsByStatus(AppointmentStatus.PENDING_CONFIRMATION);
        List<AppointmentResponseDTO> response = pending.stream().map(appointmentService::mapToDTO).toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirm an appointment", description = "Manually confirms an appointment that was pending. Requires RECEPTION role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment successfully confirmed"),
            @ApiResponse(responseCode = "400", description = "Appointment cannot be confirmed in its current state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION')")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(@PathVariable Long id) {
        Appointment confirmedAppointment = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(appointmentService.mapToDTO(confirmedAppointment));
    }
}