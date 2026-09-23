package com.harsha.food_delivery.Service;

import com.harsha.food_delivery.dto.FoodResponse;
import com.harsha.food_delivery.exception.ResourceNotFoundException;
import com.harsha.food_delivery.model.Food;
import com.harsha.food_delivery.model.Restaurant;
import com.harsha.food_delivery.repository.FoodRepository;
import com.harsha.food_delivery.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FoodService {

    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;

    public FoodService(RestaurantRepository restaurantRepository, FoodRepository foodRepository){
        this.restaurantRepository = restaurantRepository;
        this.foodRepository = foodRepository;
    }

    @Transactional
    public FoodResponse createFood(Integer restaurantId, Food food){
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                   .orElseThrow(()
                           -> new ResourceNotFoundException("Restaurant not found"));

        food.setRestaurant(restaurant);
        Food savedFood = foodRepository.save(food);
        return toFoodResponse(savedFood);
    }

    @Transactional(readOnly = true)
    public List<FoodResponse> getAllFoods(Integer restaurantId){
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() ->
                new ResourceNotFoundException("Restaurant not found"));

        List<Food> foods = foodRepository.findByRestaurant(restaurant);
        List<FoodResponse> responses = new ArrayList<>();
        for (Food food : foods) {
            responses.add(toFoodResponse(food));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public FoodResponse getFoodById(Integer foodId){
        Food food = foodRepository.findById(foodId).orElseThrow(() -> new ResourceNotFoundException("Food not found"));
        return toFoodResponse(food);
    }

    @Transactional
    public FoodResponse updateFood(Integer foodId, Food updatedFood){
        Food food = foodRepository.findById(foodId).orElseThrow(
                () -> new ResourceNotFoundException("Food not found")
        );
        food.setName(updatedFood.getName());
        food.setDescription(updatedFood.getDescription());
        food.setPrice(updatedFood.getPrice());
        food.setAvailable(updatedFood.isAvailable());
        Food savedFood = foodRepository.save(food);
        return toFoodResponse(savedFood);
    }

    @Transactional
    public void deleteFoodById(Integer foodId){
        Food food = foodRepository.findById(foodId).orElseThrow(
                () -> new ResourceNotFoundException("Food not found")
        );
        foodRepository.delete(food);
    }

    public FoodResponse toFoodResponse(Food food) {
        return new FoodResponse(
                food.getId(),
                food.getName(),
                food.getDescription(),
                food.getPrice(),
                food.isAvailable(),
                food.getRestaurant() != null ? food.getRestaurant().getId() : null,
                food.getRestaurant() != null ? food.getRestaurant().getName() : null
        );
    }
}

