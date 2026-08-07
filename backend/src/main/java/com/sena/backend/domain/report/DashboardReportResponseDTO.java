package com.sena.backend.domain.report;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardReportResponseDTO {
    private Long totalCompletedConsultations;
    private List<DiagnosisDetail> topDiagnoses;

    @Data
    @Builder
    public static class DiagnosisDetail {
        private String icd10Code;
        private Long occurrences;
    }
}