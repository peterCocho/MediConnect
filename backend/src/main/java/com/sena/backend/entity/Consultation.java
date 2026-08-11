package com.sena.backend.entity;

import com.sena.backend.domain.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "consultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultation_date", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime consultationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AppointmentStatus status;

    // Structured Core
    @Column(name = "systolic_pressure")
    private Integer systolicPressure;

    @Column(name = "diastolic_pressure")
    private Integer diastolicPressure;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "icd10_code", length = 10)
    private String icd10Code;

    // Flexible Core
    @Column(name = "reason_for_visit", columnDefinition = "TEXT")
    private String reasonForVisit;

    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    @Column(name = "management_plan", columnDefinition = "TEXT")
    private String managementPlan;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    // Relationships
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_consultation_doctor"))
    private Doctor doctor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false, foreignKey = @ForeignKey(name = "fk_consultation_mr"))
    private MedicalRecord medicalRecord;

    // Owner side of the relationship to Appointment
    // Excluded from Lombok generation to prevent StackOverflowError
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", referencedColumnName = "id", unique = true, foreignKey = @ForeignKey(name = "fk_consultation_appointment"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Appointment appointment;

}