package com.example.ecommerce_order_service.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDto {

    @NotNull(message = "UserId is required")
    private Long userId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequestDto> items;

//    // Optional: used when order is created from cart
//    private Long cartId;
}
