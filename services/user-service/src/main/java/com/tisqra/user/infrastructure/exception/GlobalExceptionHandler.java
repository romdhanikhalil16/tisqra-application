package com.tisqra.user.infrastructure.exception;

import com.tisqra.common.exception.ApiError;
import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler
 * Provides standardized error responses
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage());

        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        log.error("Business exception: {}", ex.getMessage());

        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Business Rule Violation")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation error: {}", ex.getMessage());

        List<String> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(error.getField() + ": " + error.getDefaultMessage());
        }

        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Input validation failed")
            .path(request.getRequestURI())
            .details(details)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);

        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
