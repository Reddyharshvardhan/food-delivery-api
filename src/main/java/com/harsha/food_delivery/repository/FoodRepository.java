package com.harsha.food_delivery.repository;

import com.harsha.food_delivery.model.Food;
import com.harsha.food_delivery.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food,Integer> {
    List<Food> findByRestaurant(Restaurant restaurant);
}
