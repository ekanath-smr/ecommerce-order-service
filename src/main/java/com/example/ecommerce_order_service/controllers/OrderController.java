package com.example.ecommerce_order_service.controllers;

import com.example.ecommerce_order_service.dtos.request.CreateOrderRequestDto;
import com.example.ecommerce_order_service.dtos.response.OrderResponseDto;
import com.example.ecommerce_order_service.exceptions.OrderAccessDeniedException;
import com.example.ecommerce_order_service.models.OrderStatus;
import com.example.ecommerce_order_service.security.UserPrincipal;
import com.example.ecommerce_order_service.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Order APIs", description = "Operations related to orders")
public class OrderController {

    private final OrderService orderService;

    // =========================
    // CREATE ORDER
    // =========================
    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates an order with stock reservation and confirmation")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto request,
            @RequestHeader(value = "Idempotency-Key", required = true)
            @Parameter(description = "Unique key to prevent duplicate order creation")
            @NotBlank String idempotencyKey,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (!request.getUserId().equals(principal.getUserId())) {
            throw new OrderAccessDeniedException("User mismatch");
        }
        log.info("API: Create Order called userId={}, idempotencyKey={}", principal.getUserId(), idempotencyKey);
        OrderResponseDto response = orderService.createOrder(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================
    // GET ORDER BY ID
    // =========================
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Fetch a specific order belonging to the user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("API: Get Order called orderId={}, userId={}", orderId, principal.getUserId());
        OrderResponseDto response = orderService.getOrderById(orderId, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    // =========================
    // GET ORDERS BY USER
    // =========================
    @GetMapping
    @Operation(summary = "Get all orders for user", description = "Returns paginated orders for the logged-in user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(hidden = true) @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("API: Get Orders userId={}, page={}, size={}",
                principal.getUserId(), pageable.getPageNumber(), pageable.getPageSize());
        Page<OrderResponseDto> response = orderService.getOrdersByUserId(principal.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    // =========================
    // GET BY IDEMPOTENCY KEY
    // =========================
    @GetMapping("/by-idempotency/{key}")
    @Operation(summary = "Get order by idempotency key", description = "Fetch order using idempotency key")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDto> getByIdempotencyKey(
            @PathVariable String key, @AuthenticationPrincipal UserPrincipal principal) {
        log.info("API: Get Order by idempotencyKey={}, userId={}", key, principal.getUserId());
        OrderResponseDto response = orderService.getOrderByIdempotencyKey(key, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    // =========================
    // UPDATE ORDER STATUS
    // =========================
    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update order lifecycle status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("API: Update Order Status orderId={}, status={}, userId={}", orderId, status, principal.getUserId());
        OrderResponseDto response = orderService.updateOrderStatus(orderId, status, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    // =========================
    // CANCEL ORDER
    // =========================
    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order and rolls back inventory")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("API: Cancel Order orderId={}, userId={}", orderId, principal.getUserId());
        OrderResponseDto response = orderService.cancelOrder(orderId, principal.getUserId());
        return ResponseEntity.ok(response);
    }
}