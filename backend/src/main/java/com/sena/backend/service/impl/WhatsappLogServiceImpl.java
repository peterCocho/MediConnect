package com.sena.backend.service.impl;

import com.sena.backend.domain.integration.WhatsappMessageLogDTO;
import com.sena.backend.entity.WhatsappMessageLog;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.WhatsappMessageLogRepository;
import com.sena.backend.service.WhatsappLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WhatsappLogServiceImpl implements WhatsappLogService {

    private final WhatsappMessageLogRepository messageLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WhatsappMessageLogDTO> getUnreadMessages() {
        return messageLogRepository.findByIsReadFalseOrderByReceivedAtDesc()
                .stream()
                .map(log -> WhatsappMessageLogDTO.builder()
                        .id(log.getId())
                        .phoneNumber(log.getPhoneNumber())
                        .messageBody(log.getMessageBody())
                        .receivedAt(log.getReceivedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        WhatsappMessageLog log = messageLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje de WhatsApp no encontrado con id: " + id));

        log.setIsRead(true);
        messageLogRepository.save(log);
    }
}