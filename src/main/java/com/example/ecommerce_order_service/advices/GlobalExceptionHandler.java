package com.example.ecommerce_order_service.advices;

import com.example.ecommerce_order_service.dtos.response.ErrorResponseDto;
import com.example.ecommerce_order_service.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================
    // CUSTOM EXCEPTIONS
    // =========================

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OrderAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(OrderAccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRequest(InvalidOrderRequestException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DuplicateOrderRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicate(DuplicateOrderRequestException ex) {
        log.warn("Duplicate order request: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDto> handleStock(InsufficientStockException ex) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OrderAlreadyCancelledException.class)
    public ResponseEntity<ErrorResponseDto> handleAlreadyCancelled(OrderAlreadyCancelledException ex) {
        log.warn("Order already cancelled: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OrderAlreadyDeliveredException.class)
    public ResponseEntity<ErrorResponseDto> handleAlreadyDelivered(OrderAlreadyDeliveredException ex) {
        log.warn("Order already delivered: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OrderStatusTransitionException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidTransition(OrderStatusTransitionException ex) {
        log.warn("Invalid status transition: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({InventoryServiceException.class, ProductServiceException.class})
    public ResponseEntity<ErrorResponseDto> handleInventoryService(Exception ex) {
        log.error("External service error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(feign.FeignException.class)
    public ResponseEntity<ErrorResponseDto> handleFeign(feign.FeignException ex) {
        log.error("Feign client error: status={}, message={}", ex.status(), ex.getMessage());
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return buildResponse(status, "Downstream service error");
    }

    // =========================
    // VALIDATION EXCEPTIONS
    // =========================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    // =========================
    // HEADER / REQUEST ERRORS
    // =========================

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingHeader(MissingRequestHeaderException ex) {
        log.warn("Missing header: {}", ex.getHeaderName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing required header: " + ex.getHeaderName());
    }

    // =========================
    // GENERIC FALLBACK
    // =========================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error. Please contact support.");
    }

    // =========================
    // HELPER METHODS
    // =========================

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String message) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();

        return new ResponseEntity<>(error, status);
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String message, Map<String, String> errors) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .errors(errors)
                .build();
        return new ResponseEntity<>(error, status);
    }
}