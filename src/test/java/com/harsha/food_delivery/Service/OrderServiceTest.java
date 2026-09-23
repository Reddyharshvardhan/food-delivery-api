package com.harsha.food_delivery.Service;

import com.harsha.food_delivery.dto.OrderResponse;
import com.harsha.food_delivery.exception.BadRequestException;
import com.harsha.food_delivery.model.*;
import com.harsha.food_delivery.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Restaurant restaurant;
    private Food food1;
    private Food food2;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("customer@test.com");
        testUser.setName("Customer One");
        testUser.setRole(Role.CUSTOMER);

        restaurant = new Restaurant();
        restaurant.setId(10);
        restaurant.setName("Dominos");

        food1 = new Food();
        food1.setId(101);
        food1.setName("Pepperoni Pizza");
        food1.setPrice(BigDecimal.valueOf(250));
        food1.setAvailable(true);
        food1.setRestaurant(restaurant);

        food2 = new Food();
        food2.setId(102);
        food2.setName("Garlic Bread");
        food2.setPrice(BigDecimal.valueOf(100));
        food2.setAvailable(true);
        food2.setRestaurant(restaurant);

        testCart = new Cart();
        testCart.setId(20);
        testCart.setUser(testUser);
        testCart.setCartItems(new ArrayList<>());
    }

    @Test
    @DisplayName("placeOrder: Successfully places order and calculates accurate total")
    void placeOrder_Success() {
        CartItem item1 = new CartItem();
        item1.setId(1);
        item1.setFood(food1);
        item1.setQuantity(2); // 250 * 2 = 500

        CartItem item2 = new CartItem();
        item2.setId(2);
        item2.setFood(food2);
        item2.setQuantity(1); // 100 * 1 = 100

        testCart.getCartItems().add(item1);
        testCart.getCartItems().add(item2);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(301);
            return order;
        });

        OrderResponse response = orderService.placeOrder(testUser.getEmail());

        assertNotNull(response);
        assertEquals(301, response.getId());
        assertEquals(OrderStatus.PLACED, response.getOrderStatus());
        assertEquals(BigDecimal.valueOf(600), response.getTotalAmount());
        assertEquals(restaurant.getId(), response.getRestaurantId());
        assertEquals("Dominos", response.getRestaurantName());
        assertEquals(2, response.getItems().size());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(cartItemRepository, times(1)).deleteAll(testCart.getCartItems());
    }

    @Test
    @DisplayName("placeOrder: Throws BadRequestException when cart is empty")
    void placeOrder_ThrowsException_WhenCartEmpty() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> orderService.placeOrder(testUser.getEmail()));

        assertEquals("Cart is empty", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("placeOrder: Throws BadRequestException when food item in cart becomes unavailable")
    void placeOrder_ThrowsException_WhenItemUnavailable() {
        food1.setAvailable(false); // Out of stock!

        CartItem item1 = new CartItem();
        item1.setId(1);
        item1.setFood(food1);
        item1.setQuantity(1);
        testCart.getCartItems().add(item1);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> orderService.placeOrder(testUser.getEmail()));

        assertTrue(ex.getMessage().contains("currently unavailable"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelOrder: Successfully cancels order when in PLACED status")
    void cancelOrder_Success() {
        Order order = new Order();
        order.setId(99);
        order.setUser(testUser);
        order.setRestaurant(restaurant);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setTotalAmount(BigDecimal.valueOf(500));
        order.setOrderDate(LocalDateTime.now());

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(orderRepository.findByIdAndUser(99, testUser)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(99, testUser.getEmail());

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getOrderStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("cancelOrder: Throws BadRequestException when order is not in PLACED status")
    void cancelOrder_ThrowsException_WhenNotPlaced() {
        Order order = new Order();
        order.setId(99);
        order.setUser(testUser);
        order.setRestaurant(restaurant);
        order.setOrderStatus(OrderStatus.PREPARING); // Kitchen started cooking!

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(orderRepository.findByIdAndUser(99, testUser)).thenReturn(Optional.of(order));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(99, testUser.getEmail()));

        assertEquals("Order cannot be cancelled", ex.getMessage());
    }

    @Test
    @DisplayName("updateOrderStatus: Successfully updates order status for Admin")
    void updateOrderStatus_Success() {
        Order order = new Order();
        order.setId(88);
        order.setRestaurant(restaurant);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setTotalAmount(BigDecimal.valueOf(300));
        order.setOrderDate(LocalDateTime.now());

        when(orderRepository.findById(88)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse response = orderService.updateOrderStatus(88, OrderStatus.CONFIRMED);

        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getOrderStatus());
        verify(orderRepository, times(1)).save(order);
    }
}
