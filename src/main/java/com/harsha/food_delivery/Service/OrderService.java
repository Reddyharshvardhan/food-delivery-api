package com.harsha.food_delivery.Service;

import com.harsha.food_delivery.dto.OrderItemResponse;
import com.harsha.food_delivery.dto.OrderResponse;
import com.harsha.food_delivery.exception.BadRequestException;
import com.harsha.food_delivery.exception.ResourceNotFoundException;
import com.harsha.food_delivery.model.*;
import com.harsha.food_delivery.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(UserRepository userRepository,
                        CartRepository cartRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public OrderResponse placeOrder(String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        Cart cart = cartRepository.findByUser(user).orElseThrow(
                () -> new ResourceNotFoundException("Cart not found")
        );
        if(cart.getCartItems() == null || cart.getCartItems().isEmpty()){
            throw new BadRequestException("Cart is empty");
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        Restaurant restaurant = cart.getCartItems().get(0).getFood().getRestaurant();
        for(CartItem cartItem : cart.getCartItems()){
            if(!cartItem.getFood().isAvailable()){
                throw new BadRequestException("Item '" + cartItem.getFood().getName() + "' is currently unavailable. Please remove it from your cart to proceed.");
            }
            if(!cartItem.getFood().getRestaurant().equals(restaurant)){
                throw new BadRequestException("Cart contains food from different restaurants");
            }
            BigDecimal itemTotal = cartItem.getFood().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setOrderDate(LocalDateTime.now());
        order = orderRepository.save(order);

        for(CartItem cartItem : cart.getCartItems()){
            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setFood(cartItem.getFood());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getFood().getPrice());
            orderItemRepository.save(orderItem);

            order.getOrderItems().add(orderItem);
        }
        cartItemRepository.deleteAll(cart.getCartItems());
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        List<Order> orders = orderRepository.findByUser(user);
        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orders) {
            responses.add(toOrderResponse(order));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer id, String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        Order order = orderRepository.findByIdAndUser(id, user).orElseThrow(
                () -> new ResourceNotFoundException("Order not found")
        );
        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus status){
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResourceNotFoundException("Order not found")
        );
        order.setOrderStatus(status);
        order = orderRepository.save(order);
        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Integer orderId, String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

        Order order = orderRepository.findByIdAndUser(orderId, user).orElseThrow(
                () -> new ResourceNotFoundException("Order not found")
        );

        if(order.getOrderStatus() != OrderStatus.PLACED){
            throw new BadRequestException("Order cannot be cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return toOrderResponse(order);
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderItemResponse(
                item.getId(),
                item.getFood().getId(),
                item.getFood().getName(),
                item.getQuantity(),
                item.getPrice(),
                subtotal
        );
    }

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                items.add(toOrderItemResponse(item));
            }
        }
        return new OrderResponse(
                order.getId(),
                order.getOrderStatus(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getRestaurant() != null ? order.getRestaurant().getId() : null,
                order.getRestaurant() != null ? order.getRestaurant().getName() : null,
                items
        );
    }
}

