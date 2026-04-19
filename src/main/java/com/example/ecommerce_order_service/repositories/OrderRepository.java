package com.example.ecommerce_order_service.repositories;

import com.example.ecommerce_order_service.models.Order;
import com.example.ecommerce_order_service.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
}