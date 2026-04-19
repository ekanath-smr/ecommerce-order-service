package com.example.ecommerce_order_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_user_status", columnList = "userId, status")
        }
)
@Builder
public class Order extends BaseModel {

    // From Auth Service
    @Column(nullable = false)
    private Long userId;

    // Order status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // Total price of order
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // Order Items
    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Version
    private Long version;

    @Column(unique = true, nullable = false, updatable = false)
    private String idempotencyKey;

    public void addItem(OrderItem item) {
        if (item == null) return;
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        if (item == null) return;
        items.remove(item);
        item.setOrder(null);
    }
}
