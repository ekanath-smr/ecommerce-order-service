package com.example.ecommerce_order_service.clients.productClient;

import com.example.ecommerce_order_service.clients.productClient.dtos.ProductDto;
import com.example.ecommerce_order_service.exceptions.ProductServiceException;
import com.example.ecommerce_order_service.exceptions.ProductNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductServiceClient {

    private final ProductClient productClient;

    public ProductServiceClient(ProductClient productClient) {
        this.productClient = productClient;
    }

    // ================= GET PRODUCT =================

    @Retry(name = "productService")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductDto getProduct(Long productId) {
        return productClient.getProductById(productId);
    }

    public ProductDto getProductFallback(Long productId, Throwable ex) {
        if (ex instanceof feign.FeignException.NotFound) {
            log.warn("Product not found in downstream service, productId={}", productId);
            throw new ProductNotFoundException(productId);
        }
        log.error("Product service failure: getProduct, productId={}, error={}",
                productId, ex.getMessage(), ex);
        throw new ProductServiceException(
                "Product service unavailable for productId: " + productId,
                ex
        );
    }

    // ================= VALIDATE PRODUCT =================

    @Retry(name = "productService")
    @CircuitBreaker(name = "productService", fallbackMethod = "validateProductFallback")
    public boolean validateProduct(Long productId) {
        return productClient.existsProductById(productId);
    }

//    public boolean validateProductFallback(Long productId, Throwable ex) {
//        if (ex instanceof feign.FeignException.NotFound) {
//            log.warn("Product not found during validation, productId={}", productId);
//            return false;
//        }
//        log.error("Product service failure: validateProduct, productId={}, error={}",
//                productId, ex.getMessage(), ex);
//        return false;
//    }

    public boolean validateProductFallback(Long productId, Throwable ex) {
        if (ex instanceof feign.FeignException feignEx) {
            int status = feignEx.status();
            if (status == 404) {
                log.warn("Product not found during validation, productId={}", productId);
                return false;
            }
            if (status >= 400 && status < 500) {
                log.warn("Client error from product service: {}", feignEx.contentUTF8());
                throw new ProductServiceException("Invalid request to product service", ex);
            }
            if (status >= 500) {
                log.error("Product service 5xx error", ex);
            }
        }
        log.error("Product service failure: validateProduct, productId={}", productId, ex);
        throw new ProductServiceException(
                "Product service unavailable during validation for productId: " + productId,
                ex
        );
    }

}