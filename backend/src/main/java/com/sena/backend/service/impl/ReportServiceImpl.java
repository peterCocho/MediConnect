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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ConsultationRepository consultationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardReportResponseDTO getCurrentMonthDashboard() {
        // Reports must retain completed diagnoses across months; a month-only filter made
        // the dashboard appear empty whenever the current month had no completed consultations.
        long totalConsultations = consultationRepository.countByStatus(AppointmentStatus.COMPLETED);

        List<DiagnosisCountProjection> projections = consultationRepository.findTopDiagnosesByStatus(
                AppointmentStatus.COMPLETED, PageRequest.of(0, 5));

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