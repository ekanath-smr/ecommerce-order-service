package com.example.ecommerce_order_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_id", columnList = "order_id")
        }
)
@SuperBuilder
public class OrderItem extends BaseModel {

    // Product info from Product Service
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, updatable = false)
    private String productNameSnapshot;

    // Price snapshot (VERY IMPORTANT)
    @Column(nullable = false, precision = 12, scale = 2, updatable = false)
    @DecimalMin("0.0")
    private BigDecimal unitPriceSnapshot;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    // Relation to Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}