package com.example.ecommerce_order_service.services;

import com.example.ecommerce_order_service.clients.inventoryClient.InventoryServiceClient;
import com.example.ecommerce_order_service.clients.inventoryClient.dtos.ValidateStockResponseDto;
import com.example.ecommerce_order_service.clients.productClient.ProductServiceClient;
import com.example.ecommerce_order_service.clients.productClient.dtos.ProductDto;
import com.example.ecommerce_order_service.dtos.request.CreateOrderRequestDto;
import com.example.ecommerce_order_service.dtos.request.OrderItemRequestDto;
import com.example.ecommerce_order_service.exceptions.InsufficientStockException;
import com.example.ecommerce_order_service.exceptions.OrderAccessDeniedException;
import com.example.ecommerce_order_service.models.Order;
import com.example.ecommerce_order_service.models.OrderStatus;
import com.example.ecommerce_order_service.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    // =========================
    // SUCCESS CASE
    // =========================
    @Test
    void createOrder_success() {

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setUserId(1L);

        OrderItemRequestDto item = new OrderItemRequestDto();
        item.setProductId(10L);
        item.setQuantity(2);
        request.setItems(List.of(item));

        ProductDto product = new ProductDto();
        product.setId(10L);
        product.setPrice(BigDecimal.valueOf(100));

        ValidateStockResponseDto stock = ValidateStockResponseDto.builder()
                .isStockAvailable(true)
                .build();

        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.CREATED)
                .build();

        when(productServiceClient.getProduct(10L)).thenReturn(product);
        when(inventoryServiceClient.validateStock(10L, 2)).thenReturn(stock);

        // Important: save() is called multiple times
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                o.setId(1L);
            }
            return o;
        });

        var response = orderService.createOrder(request, "idem");

        assertEquals(OrderStatus.CONFIRMED.name(), response.getStatus());

        verify(inventoryServiceClient).reserveStock(10L, 2, 1L);
        verify(inventoryServiceClient).confirmSale(10L, 2, 1L);
    }

    // =========================
    // INSUFFICIENT STOCK
    // =========================
    @Test
    void createOrder_insufficientStock() {

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setUserId(1L);

        OrderItemRequestDto item = new OrderItemRequestDto();
        item.setProductId(10L);
        item.setQuantity(5);
        request.setItems(List.of(item));

        ProductDto product = new ProductDto();
        product.setId(10L);
        product.setPrice(BigDecimal.valueOf(100));

        ValidateStockResponseDto stock = ValidateStockResponseDto.builder()
                .isStockAvailable(false)
                .availableQuantity(0)
                .build();

        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        when(productServiceClient.getProduct(10L)).thenReturn(product);
        when(inventoryServiceClient.validateStock(10L, 5)).thenReturn(stock);

        assertThrows(InsufficientStockException.class,
                () -> orderService.createOrder(request, "idem"));

        verify(inventoryServiceClient, never()).confirmSale(any(), any(), any());
    }

    // =========================
    // GET ORDER SUCCESS
    // =========================
    @Test
    void getOrder_success() {

        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var response = orderService.getOrderById(1L, 1L);

        assertEquals(OrderStatus.CONFIRMED.name(), response.getStatus());
    }

    // =========================
    // UNAUTHORIZED
    // =========================
    @Test
    void getOrder_unauthorized() {

        Order order = Order.builder()
                .id(1L)
                .userId(2L)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(
                OrderAccessDeniedException.class,
                () -> orderService.getOrderById(1L, 1L)
        );
    }

    // =========================
    // CANCEL ORDER
    // =========================
    @Test
    void cancelOrder_success() {

        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .items(List.of())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        var response = orderService.cancelOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED.name(), response.getStatus());
    }
}