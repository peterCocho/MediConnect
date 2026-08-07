package com.sena.backend.service;

import com.sena.backend.domain.report.DashboardReportResponseDTO;

public interface ReportService {
    DashboardReportResponseDTO getCurrentMonthDashboard();
}