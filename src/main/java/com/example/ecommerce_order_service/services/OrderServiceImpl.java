package com.example.ecommerce_order_service.services;

import com.example.ecommerce_order_service.clients.inventoryClient.InventoryServiceClient;
import com.example.ecommerce_order_service.clients.inventoryClient.dtos.ValidateStockResponseDto;
import com.example.ecommerce_order_service.clients.productClient.ProductServiceClient;
import com.example.ecommerce_order_service.clients.productClient.dtos.ProductDto;
import com.example.ecommerce_order_service.dtos.request.CreateOrderRequestDto;
import com.example.ecommerce_order_service.dtos.request.OrderItemRequestDto;
import com.example.ecommerce_order_service.dtos.response.OrderResponseDto;
import com.example.ecommerce_order_service.exceptions.*;
import com.example.ecommerce_order_service.mappers.OrderMapper;
import com.example.ecommerce_order_service.models.Order;
import com.example.ecommerce_order_service.models.OrderItem;
import com.example.ecommerce_order_service.models.OrderStatus;
import com.example.ecommerce_order_service.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.ecommerce_order_service.mappers.OrderMapper.toOrderItemEntity;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;

    // =========================
    // CREATE ORDER
    // =========================
    // “This is a Synchronous compensating transaction pattern, but ideally this should be implemented using Saga pattern (orchestrated/event-driven).”
    // Saga -> persistent state transitions and asynchronous communication to ensure reliability and fault tolerance
    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto request, String idempotencyKey) {

        log.info("Creating order for userId={}, idempotencyKey={}", request != null ? request.getUserId() : null, idempotencyKey);

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderRequestException("Order items cannot be empty");
        }
        Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .idempotencyKey(idempotencyKey)
                .build();
        try {
            order = orderRepository.save(order);
            log.info("Order created with id={} (CREATED state)", order.getId());
        } catch (DataIntegrityViolationException ex) {
            log.warn("Duplicate order request detected for idempotencyKey={}", idempotencyKey);
            throw new DuplicateOrderRequestException("Duplicate order request");
        }
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> reservedItems = new ArrayList<>();
        List<OrderItem> soldItems = new ArrayList<>();
        OrderItem orderItem; ProductDto productDto;
        ValidateStockResponseDto stockResponse;
        try {
            for (OrderItemRequestDto itemRequest : request.getItems()) {
                log.info("Processing item productId={}, quantity={}", itemRequest.getProductId(), itemRequest.getQuantity());
                productDto = productServiceClient.getProduct(itemRequest.getProductId());
                stockResponse = inventoryServiceClient.validateStock(
                        itemRequest.getProductId(), itemRequest.getQuantity()
                );
                if (!stockResponse.getIsStockAvailable()) {
                    log.warn("Insufficient stock for productId={}, requested={}, available={}",
                            itemRequest.getProductId(), itemRequest.getQuantity(), stockResponse.getAvailableQuantity());
                    throw new InsufficientStockException(itemRequest.getProductId(), itemRequest.getQuantity(), stockResponse.getAvailableQuantity());
                }
                inventoryServiceClient.reserveStock(itemRequest.getProductId(), itemRequest.getQuantity(), order.getId());
                log.info("Stock reserved for productId={}, quantity={}", itemRequest.getProductId(), itemRequest.getQuantity());
                orderItem = toOrderItemEntity(
                        productDto.getId(), productDto.getTitle(),
                        productDto.getPrice(), itemRequest.getQuantity()
                );
                reservedItems.add(orderItem);
                order.addItem(orderItem);
                total = total.add(
                        productDto.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                );
            }
            order.setTotalAmount(total);
            order.setStatus(OrderStatus.RESERVED);
            order = orderRepository.save(order);
            log.info("Order id={} total calculated: {}", order.getId(), total);
            // TODO: Integrate PaymentService (authorize -> capture)
            for (OrderItem item : order.getItems()) {
                inventoryServiceClient.confirmSale(item.getProductId(), item.getQuantity(), order.getId());
                soldItems.add(item);
                log.info("Stock confirmed (sold) for productId={}, quantity={}", item.getProductId(), item.getQuantity());
            }
            order.setStatus(OrderStatus.CONFIRMED);
            order = orderRepository.save(order);
            log.info("Order id={} moved to CONFIRMED", order.getId());
            return OrderMapper.mapOrderToOrderResponseDto(order);
        } catch (Exception e) {
            log.error("Order creation failed for orderId={}, starting rollback", order.getId(), e);
            undoSoldItems(soldItems);
            undoReservedItems(reservedItems);
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw e;
        }
    }

    // =========================
    // GET ORDER BY ID
    // =========================
    @Override
    public OrderResponseDto getOrderById(Long orderId, Long userId) {
        log.info("Fetching order by id={}, userId={}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getUserId().equals(userId)) {
            log.warn("Unauthorized access to orderId={} by userId={}", orderId, userId);
            throw new OrderAccessDeniedException(orderId);
        }
        return OrderMapper.mapOrderToOrderResponseDto(order);
    }

    // =========================
    // GET ORDERS BY USER
    // =========================
    @Override
    public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {
        log.info("Fetching orders for userId={}", userId);
        return orderRepository.findByUserId(userId, pageable)
                .map(OrderMapper::mapOrderToOrderResponseDto);
    }

    // =========================
    // GET BY IDEMPOTENCY KEY
    // =========================
    @Override
    public OrderResponseDto getOrderByIdempotencyKey(String key, Long userId) {
        log.info("Fetching order by idempotencyKey={}, userId={}", key, userId);
        Order order = orderRepository.findByIdempotencyKey(key)
                .orElseThrow(() -> {
                    log.warn("Order not found for idempotencyKey={}", key);
                    return new OrderNotFoundException(key);
                });
        if (!order.getUserId().equals(userId)) {
            log.warn("Unauthorized access to orderId={} via idempotencyKey", order.getId());
            throw new OrderAccessDeniedException(order.getId());
        }
        return OrderMapper.mapOrderToOrderResponseDto(order);
    }

    // =========================
    // UPDATE STATUS
    // =========================
    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus, Long userId) {
        log.info("Updating order status orderId={}, newStatus={}, userId={}", orderId, newStatus, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
//        if (!order.getUserId().equals(userId)) {
//            log.warn("Unauthorized status update attempt orderId={}", orderId);
//            throw new OrderAccessDeniedException(orderId);
//        }
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        return OrderMapper.mapOrderToOrderResponseDto(orderRepository.save(order));
    }

    // =========================
    // CANCEL ORDER
    // =========================
    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order orderId={}, userId={}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getUserId().equals(userId)) {
            log.warn("Unauthorized cancel attempt orderId={}", orderId);
            throw new OrderAccessDeniedException(orderId);
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Order already cancelled orderId={}", orderId);
            throw new OrderAlreadyCancelledException(orderId);
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            log.warn("Cannot cancel delivered orderId={}", orderId);
            throw new OrderAlreadyDeliveredException(orderId);
        }
        for (OrderItem item : order.getItems()) {
            inventoryServiceClient.undoConfirmedSale(item.getProductId(), item.getQuantity(), orderId);
            inventoryServiceClient.releaseStock(item.getProductId(), item.getQuantity(), orderId);
        }
        order.setStatus(OrderStatus.CANCELLED);
        log.info("Order cancelled successfully orderId={}", orderId);
        return OrderMapper.mapOrderToOrderResponseDto(orderRepository.save(order));
    }

    // =========================
    // STATUS VALIDATION
    // =========================
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        switch (current) {
            case CREATED -> {
                if (next != OrderStatus.CONFIRMED && next != OrderStatus.CANCELLED) {
                    throw new OrderStatusTransitionException(current, next);
                }
            }
            case CONFIRMED -> {
                if (next != OrderStatus.SHIPPED && next != OrderStatus.CANCELLED) {
                    throw new OrderStatusTransitionException(current, next);
                }
            }
            case SHIPPED -> {
                if (next != OrderStatus.DELIVERED) {
                    throw new OrderStatusTransitionException(current, next);
                }
            }
            default -> throw new OrderStatusTransitionException(current, next);
        }
    }

    private void undoReservedItems(List<OrderItem> reservedItems) {
        for(OrderItem oi : reservedItems) {
            log.info("Releasing reserved stock for productId={}, quantity={}", oi.getProductId(), oi.getQuantity());
            try {
                inventoryServiceClient.releaseStock(oi.getProductId(), oi.getQuantity(), oi.getOrder().getId());
            } catch (Exception ex) {
                log.error("CRITICAL: Failed to release stock for productId={}, quantity={}, orderId={}, manual intervention required",
                        oi.getProductId(), oi.getQuantity(), oi.getOrder().getId());
            }
        }
    }

    private void undoSoldItems(List<OrderItem> soldItems) {
        for(OrderItem oi : soldItems) {
            log.info("Undoing confirmed sale for productId={}, quantity={}", oi.getProductId(), oi.getQuantity());
            try {
                inventoryServiceClient.undoConfirmedSale(oi.getProductId(), oi.getQuantity(), oi.getOrder().getId());
            } catch (Exception ex) {
                log.error("CRITICAL: Failed to undo sale for productId={}, quantity={}, orderId={}, manual intervention required",
                        oi.getProductId(), oi.getQuantity(), oi.getOrder().getId());
            }
        }
    }
}