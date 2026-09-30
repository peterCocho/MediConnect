package com.sena.backend.integration;

import org.springframework.boot.test.context.SpringBootTest;
import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.entity.Patient;
import com.sena.backend.entity.Role;
import com.sena.backend.entity.User;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.MedicalRecordRepository;
import com.sena.backend.repository.PatientRepository;
import com.sena.backend.repository.RoleRepository;
import com.sena.backend.repository.UserRepository;
import com.sena.backend.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de concurrencia (Testcontainers + PostgreSQL real).
 *
 * Escenario del plan de pruebas: N usuarios intentan reservar el mismo
 * bloque de cita (mismo médico, mismo horario) al mismo tiempo. Solo uno
 * debe persistir la cita; el resto debe recibir un error de negocio
 * (BusinessRuleException, que el GlobalExceptionHandler mapea a 409
 * CONFLICT en la capa HTTP — aquí se prueba directo contra el servicio y
 * la base de datos, no contra el controlador).
 *
 * AppointmentServiceImpl.bookAppointment ya usa un lock pesimista
 * (DoctorRepository.findByIdWithLock → SELECT ... FOR UPDATE) antes de
 * validar solapamiento, así que esta prueba verifica que ese mecanismo
 * sostiene la garantía bajo concurrencia real con hilos y transacciones
 * separadas, algo que Mockito no puede probar.
 */
@SpringBootTest
@DisplayName("Concurrencia: agendamiento simultáneo del mismo bloque de cita")
class AppointmentConcurrencyIT extends AbstractIntegrationTest {

    private static final int CONCURRENT_PATIENTS = 10;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private Argon2PasswordEncoder passwordEncoder;

    private Long doctorId;
    private List<Long> patientIds;
    private OffsetDateTime start;
    private OffsetDateTime end;

    @BeforeEach
    void setUp() {
        // Fetch the existing role to avoid unique constraint violations, or create it if not found
        Role doctorRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_DOCTOR".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_DOCTOR").description("Médico").build()));

        User doctorUser = userRepository.save(User.builder()
                .username("dr.concurrencia")
                .password(passwordEncoder.encode("Secret123"))
                .role(doctorRole)
                .isActive(true)
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .documentNumber("DOC-CONC-1")
                .fullName("Dra. Concurrencia Test")
                .email("concurrencia@clinica.com")
                .phone("573000000000")
                .specialty("Medicina General")
                .user(doctorUser)
                .build());
        doctorId = doctor.getId();

        patientIds = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_PATIENTS; i++) {
            Patient patient = patientRepository.save(Patient.builder()
                    .identityDocument("CONC-PAT-" + i)
                    .fullName("Paciente Concurrencia " + i)
                    .phone("57300000" + String.format("%04d", i))
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .isActive(true)
                    .build());
            medicalRecordRepository.save(MedicalRecord.builder().patient(patient).build());
            patientIds.add(patient.getId());
        }

        // Lunes (día hábil) 10:00–10:30 hora Bogotá, dentro del horario de atención.
        start = OffsetDateTime.of(2026, 10, 5, 10, 0, 0, 0, ZoneOffset.of("-05:00"));
        end = start.plusMinutes(30);
    }

    @Test
    @DisplayName("solo un paciente logra agendar el bloque; el resto recibe BusinessRuleException y no queda duplicado en la BD")
    void bookAppointment_withConcurrentRequestsForSameSlot_onlyOnePersists() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_PATIENTS);
        CountDownLatch readyLatch = new CountDownLatch(CONCURRENT_PATIENTS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_PATIENTS);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger businessRuleConflicts = new AtomicInteger(0);
        List<Throwable> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());

        for (Long patientId : patientIds) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    // Todos los hilos esperan aquí para intentar reservar al mismo tiempo.
                    startLatch.await();

                    appointmentService.bookAppointment(patientId, doctorId, start, end);
                    successCount.incrementAndGet();
                } catch (BusinessRuleException expectedConflict) {
                    businessRuleConflicts.incrementAndGet();
                } catch (Throwable unexpected) {
                    unexpectedErrors.add(unexpected);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(); // espera a que los 10 hilos estén listos en la línea de salida
        startLatch.countDown(); // dispara los 10 intentos simultáneamente
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).as("los %d hilos debieron terminar dentro del tiempo límite", CONCURRENT_PATIENTS).isTrue();
        assertThat(unexpectedErrors).as("no debieron ocurrir excepciones distintas a BusinessRuleException").isEmpty();

        assertThat(successCount.get()).as("exactamente un paciente debe lograr agendar el bloque").isEqualTo(1);
        assertThat(businessRuleConflicts.get()).as("los demás %d intentos deben rechazarse por solapamiento", CONCURRENT_PATIENTS - 1)
                .isEqualTo(CONCURRENT_PATIENTS - 1);

        // Confirmación a nivel de base de datos: solo debe existir una cita para ese médico en ese horario.
        List<Appointment> appointmentsForDoctor = appointmentRepository.findByStatus(AppointmentStatus.PENDING_CONFIRMATION)
                .stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .filter(a -> a.getStartTime().isEqual(start))
                .toList();
        assertThat(appointmentsForDoctor).hasSize(1);
    }
}