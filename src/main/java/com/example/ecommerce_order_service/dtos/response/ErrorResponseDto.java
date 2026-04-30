package com.example.ecommerce_order_service.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponseDto {
    private Map<String, String> errors;
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
}