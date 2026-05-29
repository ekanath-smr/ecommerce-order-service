package com.example.ecommerce_order_service.clients.productClient;

import com.example.ecommerce_order_service.clients.productClient.dtos.ProductDto;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "ECOMMERCE-PRODUCT-SERVICE",
        url = "${product.service.url:}"
)
public interface ProductClient {

    @GetMapping("/products/{productId}")
    ProductDto getProductById(@PathVariable @Positive Long productId);

    @GetMapping("/products/{productId}/exists")
    Boolean existsProductById(@PathVariable @Positive Long productId);
}
