package com.harsha.food_delivery.Service;

import com.harsha.food_delivery.dto.CartItemResponse;
import com.harsha.food_delivery.dto.CartResponse;
import com.harsha.food_delivery.exception.BadRequestException;
import com.harsha.food_delivery.model.*;
import com.harsha.food_delivery.repository.CartItemRepository;
import com.harsha.food_delivery.repository.CartRepository;
import com.harsha.food_delivery.repository.FoodRepository;
import com.harsha.food_delivery.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Restaurant restaurant1;
    private Restaurant restaurant2;
    private Food food1;
    private Food food2;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("customer@test.com");
        testUser.setName("First Customer");
        testUser.setRole(Role.CUSTOMER);

        restaurant1 = new Restaurant();
        restaurant1.setId(1);
        restaurant1.setName("KFC Wings");

        restaurant2 = new Restaurant();
        restaurant2.setId(2);
        restaurant2.setName("Dominos King");

        food1 = new Food();
        food1.setId(101);
        food1.setName("Bucket chicken wings");
        food1.setPrice(BigDecimal.valueOf(500));
        food1.setAvailable(true);
        food1.setRestaurant(restaurant1);

        food2 = new Food();
        food2.setId(102);
        food2.setName("Garlic Pizza");
        food2.setPrice(BigDecimal.valueOf(200));
        food2.setAvailable(true);
        food2.setRestaurant(restaurant2);

        testCart = new Cart();
        testCart.setId(10);
        testCart.setUser(testUser);
        testCart.setCartItems(new ArrayList<>());
    }

    @Test
    @DisplayName("addToCart: Successfully adds new food item to empty cart")
    void addToCart_Success_NewItem() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(foodRepository.findById(food1.getId())).thenReturn(Optional.of(food1));
        when(cartItemRepository.findByCartAndFood(testCart, food1)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(501);
            return item;
        });

        CartItemResponse response = cartService.addToCart(testUser.getEmail(), food1.getId(), 2);

        assertNotNull(response);
        assertEquals(food1.getId(), response.getFoodId());
        assertEquals("Bucket chicken wings", response.getFoodName());
        assertEquals(2, response.getQuantity());
        assertEquals(BigDecimal.valueOf(1000), response.getSubtotal());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addToCart: Increments quantity when item already in cart")
    void addToCart_Success_ExistingItem() {
        CartItem existingItem = new CartItem();
        existingItem.setId(501);
        existingItem.setCart(testCart);
        existingItem.setFood(food1);
        existingItem.setQuantity(2);
        testCart.getCartItems().add(existingItem);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(foodRepository.findById(food1.getId())).thenReturn(Optional.of(food1));
        when(cartItemRepository.findByCartAndFood(testCart, food1)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingItem);

        CartItemResponse response = cartService.addToCart(testUser.getEmail(), food1.getId(), 3);

        assertNotNull(response);
        assertEquals(5, response.getQuantity());
        assertEquals(BigDecimal.valueOf(2500), response.getSubtotal());
    }

    @Test
    @DisplayName("addToCart: Throws BadRequestException when quantity <= 0")
    void addToCart_ThrowsException_WhenQuantityInvalid() {
        assertThrows(BadRequestException.class, () -> cartService.addToCart(testUser.getEmail(), food1.getId(), 0));
        assertThrows(BadRequestException.class, () -> cartService.addToCart(testUser.getEmail(), food1.getId(), -5));
        verifyNoInteractions(cartRepository);
    }

    @Test
    @DisplayName("addToCart: Throws BadRequestException when food is not available")
    void addToCart_ThrowsException_WhenFoodNotAvailable() {
        food1.setAvailable(false);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(foodRepository.findById(food1.getId())).thenReturn(Optional.of(food1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cartService.addToCart(testUser.getEmail(), food1.getId(), 1));

        assertEquals("Food is not available", ex.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToCart: Throws BadRequestException when adding food from a different restaurant")
    void addToCart_ThrowsException_WhenDifferentRestaurant() {
        CartItem existingItem = new CartItem();
        existingItem.setId(501);
        existingItem.setCart(testCart);
        existingItem.setFood(food1); // From Pizza Hut
        existingItem.setQuantity(1);
        testCart.getCartItems().add(existingItem);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(foodRepository.findById(food2.getId())).thenReturn(Optional.of(food2)); // From Burger King

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cartService.addToCart(testUser.getEmail(), food2.getId(), 1));

        assertTrue(ex.getMessage().contains("KFC Wings"));
        assertTrue(ex.getMessage().contains("Dominos King"));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCart: Returns cart with computed total price")
    void getCart_Success() {
        CartItem item1 = new CartItem();
        item1.setId(1);
        item1.setFood(food1);
        item1.setQuantity(2); // 150 * 2 = 300

        testCart.getCartItems().add(item1);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        CartResponse response = cartService.getCart(testUser.getEmail());

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(BigDecimal.valueOf(1000), response.getTotalCartPrice());
    }

    @Test
    @DisplayName("clearCart: Removes all items from user cart")
    void clearCart_Success() {
        CartItem item1 = new CartItem();
        item1.setId(1);
        item1.setFood(food1);
        item1.setQuantity(2);
        testCart.getCartItems().add(item1);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        cartService.clearCart(testUser.getEmail());

        verify(cartItemRepository, times(1)).deleteAll(anyList());
        assertTrue(testCart.getCartItems().isEmpty());
    }
}
