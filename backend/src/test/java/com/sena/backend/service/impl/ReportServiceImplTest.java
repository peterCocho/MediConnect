package com.sena.backend.service.impl;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.report.DashboardReportResponseDTO;
import com.sena.backend.domain.report.DiagnosisCountProjection;
import com.sena.backend.repository.ConsultationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReportServiceImpl.
 *
 * El dashboard cuenta TODAS las consultas COMPLETED (sin filtro por mes, a
 * pesar del nombre del método) y expone los 5 diagnósticos CIE-10 más
 * frecuentes que entrega el repositorio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImpl")
class ReportServiceImplTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private DiagnosisCountProjection projection(String code, long count) {
        return new DiagnosisCountProjection() {
            @Override
            public String getIcd10Code() {
                return code;
            }

            @Override
            public Long getCount() {
                return count;
            }
        };
    }

    @Test
    @DisplayName("getCurrentMonthDashboard: retorna el total de consultas COMPLETED y mapea los diagnósticos más frecuentes")
    void getCurrentMonthDashboard_returnsTotalAndMappedTopDiagnoses() {
        when(consultationRepository.countByStatus(AppointmentStatus.COMPLETED)).thenReturn(42L);
        when(consultationRepository.findTopDiagnosesByStatus(eq(AppointmentStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(List.of(projection("J06.9", 15L), projection("I10", 9L), projection("E11.9", 4L)));

        DashboardReportResponseDTO dashboard = reportService.getCurrentMonthDashboard();

        assertThat(dashboard.getTotalCompletedConsultations()).isEqualTo(42L);
        assertThat(dashboard.getTopDiagnoses()).hasSize(3);
        assertThat(dashboard.getTopDiagnoses()).extracting(DashboardReportResponseDTO.DiagnosisDetail::getIcd10Code)
                .containsExactly("J06.9", "I10", "E11.9");
        assertThat(dashboard.getTopDiagnoses()).extracting(DashboardReportResponseDTO.DiagnosisDetail::getOccurrences)
                .containsExactly(15L, 9L, 4L);
    }

    @Test
    @DisplayName("getCurrentMonthDashboard: solicita al repositorio solo los 5 diagnósticos principales")
    void getCurrentMonthDashboard_requestsTopFiveDiagnosesOnly() {
        when(consultationRepository.countByStatus(AppointmentStatus.COMPLETED)).thenReturn(0L);
        when(consultationRepository.findTopDiagnosesByStatus(eq(AppointmentStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(List.of());

        reportService.getCurrentMonthDashboard();

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(consultationRepository).findTopDiagnosesByStatus(eq(AppointmentStatus.COMPLETED), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("getCurrentMonthDashboard: no filtra por rango de fechas (conserva diagnósticos de meses anteriores)")
    void getCurrentMonthDashboard_doesNotFilterByDateRange() {
        when(consultationRepository.countByStatus(AppointmentStatus.COMPLETED)).thenReturn(10L);
        when(consultationRepository.findTopDiagnosesByStatus(eq(AppointmentStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(List.of());

        reportService.getCurrentMonthDashboard();

        verify(consultationRepository, never())
                .countByStatusAndConsultationDateBetween(any(), any(), any());
    }

    @Test
    @DisplayName("getCurrentMonthDashboard: sin consultas completadas retorna total 0 y lista de diagnósticos vacía")
    void getCurrentMonthDashboard_withNoData_returnsEmptyDashboard() {
        when(consultationRepository.countByStatus(AppointmentStatus.COMPLETED)).thenReturn(0L);
        when(consultationRepository.findTopDiagnosesByStatus(eq(AppointmentStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(List.of());

        DashboardReportResponseDTO dashboard = reportService.getCurrentMonthDashboard();

        assertThat(dashboard.getTotalCompletedConsultations()).isZero();
        assertThat(dashboard.getTopDiagnoses()).isEmpty();
    }
}
