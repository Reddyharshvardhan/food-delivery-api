package com.harsha.food_delivery.Controller;

import com.harsha.food_delivery.Service.OrderService;
import com.harsha.food_delivery.dto.OrderResponse;
import com.harsha.food_delivery.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse placeOrder(Authentication authentication) {
        return orderService.placeOrder(authentication.getName());
    }

    @GetMapping
    public List<OrderResponse> getOrders(Authentication authentication) {
        return orderService.getOrders(authentication.getName());
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@PathVariable Integer orderId,
                                      Authentication authentication) {
        return orderService.getOrderById(orderId, authentication.getName());
    }

    @PutMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(@PathVariable Integer orderId,
                                           @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(
                orderId,
                request.getStatus());
    }

    @PutMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(@PathVariable Integer orderId,
                                     Authentication authentication) {
        return orderService.cancelOrder(orderId, authentication.getName());
    }
}


