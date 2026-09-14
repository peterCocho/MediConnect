package com.sena.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "error_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exact time the exception occurred
    @Column(nullable = false)
    private OffsetDateTime timestamp;

    // HTTP status code sent back to the client
    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    // The name of the thrown exception class
    @Column(name = "exception_type", nullable = false)
    private String exceptionType;

    // Specific exception message
    @Column(columnDefinition = "TEXT")
    private String message;

    // Full stack trace for deep debugging
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    // Endpoint URL where the error was triggered
    @Column(length = 255)
    private String path;
}