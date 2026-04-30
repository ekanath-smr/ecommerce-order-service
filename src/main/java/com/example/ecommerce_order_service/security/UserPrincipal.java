package com.example.ecommerce_order_service.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String userName;
}
