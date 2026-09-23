package com.harsha.food_delivery.repository;

import com.harsha.food_delivery.model.Cart;
import com.harsha.food_delivery.model.Food;
import com.harsha.food_delivery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Integer> {
    Optional<Cart> findByUser(User user);
}
