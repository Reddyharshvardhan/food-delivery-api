package com.harsha.food_delivery.repository;

import com.harsha.food_delivery.model.Cart;
import com.harsha.food_delivery.model.CartItem;
import com.harsha.food_delivery.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
    Optional<CartItem> findByCartAndFood(Cart cart, Food food);
    Optional<CartItem> findByIdAndCart(Integer cartItemId, Cart cart);
}
