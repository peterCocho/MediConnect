package com.sena.backend.exception;

import com.sena.backend.domain.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ErrorLogRepository injection and persistError method removed.
    // Technical exceptions will no longer pollute the receptionist's dashboard.

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleInactiveUser(UsernameNotFoundException ex, HttpServletRequest request) {
        ErrorResponseDTO body = new ErrorResponseDTO("Acceso denegado: La cuenta del doctor se encuentra inactiva. Contacte al administrador.", OffsetDateTime.now(), HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponseDTO body = new ErrorResponseDTO("Credenciales incorrectas. Verifique su usuario y contraseña.", OffsetDateTime.now(), HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponseDTO body = new ErrorResponseDTO(ex.getMessage(), OffsetDateTime.now(), HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(com.sena.backend.exception.BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(com.sena.backend.exception.BusinessRuleException ex, HttpServletRequest request) {
        ErrorResponseDTO body = new ErrorResponseDTO(ex.getMessage(), OffsetDateTime.now(), HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        ValidationErrorResponseDTO body = new ValidationErrorResponseDTO("Error de validación en los datos enviados", OffsetDateTime.now(), HttpStatus.BAD_REQUEST.value(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponseDTO body = new ErrorResponseDTO("Acceso denegado: No tienes los permisos necesarios para realizar esta acción", OffsetDateTime.now(), HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, HttpServletRequest request) {
        // Print the stack trace in the console for developers only
        ex.printStackTrace();

        ErrorResponseDTO body = new ErrorResponseDTO("Ha ocurrido un error interno en el servidor", OffsetDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    public static class ValidationErrorResponseDTO extends ErrorResponseDTO {
        private final Map<String, String> fieldErrors;

        public ValidationErrorResponseDTO(String message, OffsetDateTime timestamp, int status, Map<String, String> fieldErrors) {
            super(message, timestamp, status);
            this.fieldErrors = fieldErrors;
        }

        public Map<String, String> getFieldErrors() {
            return fieldErrors;
        }
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        ErrorResponseDTO body = new ErrorResponseDTO("El formato de los datos enviados es incorrecto. Verifique las fechas.", OffsetDateTime.now(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}