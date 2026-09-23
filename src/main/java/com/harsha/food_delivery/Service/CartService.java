package com.harsha.food_delivery.Service;

import com.harsha.food_delivery.dto.CartItemResponse;
import com.harsha.food_delivery.dto.CartResponse;
import com.harsha.food_delivery.exception.BadRequestException;
import com.harsha.food_delivery.exception.ResourceNotFoundException;
import com.harsha.food_delivery.model.Cart;
import com.harsha.food_delivery.model.CartItem;
import com.harsha.food_delivery.model.Food;
import com.harsha.food_delivery.model.Restaurant;
import com.harsha.food_delivery.model.User;
import com.harsha.food_delivery.repository.CartItemRepository;
import com.harsha.food_delivery.repository.CartRepository;
import com.harsha.food_delivery.repository.FoodRepository;
import com.harsha.food_delivery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       FoodRepository foodRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartItemResponse addToCart(String email, Integer foodId, Integer quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        Cart cart = cartRepository.findByUser(user).orElseGet(
                () -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                }
        );

        Food food = foodRepository.findById(foodId).orElseThrow(
                () -> new ResourceNotFoundException("Food not found")
        );
        if (!food.isAvailable()) {
            throw new BadRequestException("Food is not available");
        }

        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            Restaurant existingRestaurant = cart.getCartItems().get(0).getFood().getRestaurant();
            if (!existingRestaurant.getId().equals(food.getRestaurant().getId())) {
                throw new BadRequestException("Your cart contains items from " + existingRestaurant.getName() +
                        ". Please clear your cart before adding items from " + food.getRestaurant().getName() + ".");
            }
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndFood(cart, food);

        CartItem savedItem;
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            savedItem = cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setFood(food);
            cartItem.setQuantity(quantity);
            savedItem = cartItemRepository.save(cartItem);
        }
        return toCartItemResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        Cart cart = cartRepository.findByUser(user).orElseThrow(
                () -> new ResourceNotFoundException("Cart not found")
        );
        return toCartResponse(cart);
    }

    @Transactional
    public CartItemResponse updateCartItem(
            String email,
            Integer cartItemId,
            Integer quantity
    ) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        Cart cart = cartRepository.findByUser(user).orElseThrow(
                () -> new ResourceNotFoundException("Cart not found")
        );

        CartItem cartItem = cartItemRepository.findByIdAndCart(cartItemId, cart).orElseThrow(
                () -> new ResourceNotFoundException("Cart item not found")
        );
        cartItem.setQuantity(quantity);
        CartItem savedItem = cartItemRepository.save(cartItem);
        return toCartItemResponse(savedItem);
    }

    @Transactional
    public void removeCartItem(String email, Integer cartItemId) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        Cart cart = cartRepository.findByUser(user).orElseThrow(
                () -> new ResourceNotFoundException("Cart not found")
        );
        CartItem cartItem = cartItemRepository.findByIdAndCart(cartItemId, cart).orElseThrow(
                () -> new ResourceNotFoundException("Cart item not found")
        );
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        Cart cart = cartRepository.findByUser(user).orElseThrow(
                () -> new ResourceNotFoundException("Cart not found")
        );
        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            cartItemRepository.deleteAll(cart.getCartItems());
            cart.getCartItems().clear();
        }
    }

    public CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getFood().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                item.getFood().getId(),
                item.getFood().getName(),
                item.getFood().getPrice(),
                item.getQuantity(),
                subtotal
        );
    }

    public CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                CartItemResponse itemResponse = toCartItemResponse(item);
                items.add(itemResponse);
                total = total.add(itemResponse.getSubtotal());
            }
        }
        return new CartResponse(cart.getId(), items, total);
    }
}