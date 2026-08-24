package com.sena.backend.service.impl;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.report.DashboardReportResponseDTO;
import com.sena.backend.domain.report.DiagnosisCountProjection;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ConsultationRepository consultationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardReportResponseDTO getCurrentMonthDashboard() {
        OffsetDateTime now = OffsetDateTime.now();

        // Calculate boundaries for the current month
        OffsetDateTime startOfMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // Execute count metric
        long totalConsultations = consultationRepository.countByStatusAndConsultationDateBetween(
                AppointmentStatus.COMPLETED, startOfMonth, endOfMonth);

        // Execute grouping metric (limit to top 5)
        List<DiagnosisCountProjection> projections = consultationRepository.findTopDiagnosesByDateRange(
                AppointmentStatus.COMPLETED, startOfMonth, endOfMonth, PageRequest.of(0, 5));

        // Map projections to DTO
        List<DashboardReportResponseDTO.DiagnosisDetail> topDiagnoses = projections.stream()
                .map(p -> DashboardReportResponseDTO.DiagnosisDetail.builder()
                        .icd10Code(p.getIcd10Code())
                        .occurrences(p.getCount())
                        .build())
                .collect(Collectors.toList());

        return DashboardReportResponseDTO.builder()
                .totalCompletedConsultations(totalConsultations)
                .topDiagnoses(topDiagnoses)
                .build();
    }
}