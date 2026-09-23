package com.harsha.food_delivery.Controller;

import com.harsha.food_delivery.Service.CartService;
import com.harsha.food_delivery.dto.AddToCartRequest;
import com.harsha.food_delivery.dto.CartItemResponse;
import com.harsha.food_delivery.dto.CartResponse;
import com.harsha.food_delivery.dto.UpdateCartItemRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public CartItemResponse addToCart(@Valid @RequestBody AddToCartRequest request,
                                      Authentication authentication) {
        return cartService.addToCart(authentication.getName(), request.getFoodId(), request.getQuantity());
    }

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        return cartService.getCart(authentication.getName());
    }

    @PutMapping("/items/{cartItemId}")
    public CartItemResponse updateCartItem(@PathVariable Integer cartItemId,
                                           @Valid @RequestBody UpdateCartItemRequest request,
                                           Authentication authentication) {
        return cartService.updateCartItem(authentication.getName(), cartItemId, request.getQuantity());
    }

    @DeleteMapping("/items/{cartItemId}")
    public void removeCartItem(@PathVariable Integer cartItemId,
                               Authentication authentication) {
        cartService.removeCartItem(authentication.getName(), cartItemId);
    }

    @DeleteMapping
    public void clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
    }
}


