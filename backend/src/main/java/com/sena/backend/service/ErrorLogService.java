package com.sena.backend.service;

import com.sena.backend.entity.ErrorLog;
import com.sena.backend.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    // Forces a new transaction, bypassing AFTER_COMMIT restrictions
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveIntegrationError(String module, String patientName, String phone, String errorMessage) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setTimestamp(OffsetDateTime.now());
        errorLog.setExceptionType("Fallo Crítico de Conexión");
        errorLog.setPath(module);

        String formattedMessage = String.format("Fallo al contactar paciente %s (%s). %s",
                patientName, phone, errorMessage);
        errorLog.setMessage(formattedMessage);

        errorLogRepository.save(errorLog);
    }
}