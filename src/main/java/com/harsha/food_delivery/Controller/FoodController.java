package com.harsha.food_delivery.Controller;

import com.harsha.food_delivery.Service.FoodService;
import com.harsha.food_delivery.dto.FoodResponse;
import com.harsha.food_delivery.model.Food;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService){
        this.foodService = foodService;
    }

    @PostMapping("/{restaurantId}/foods")
    public FoodResponse createFood(@PathVariable Integer restaurantId, @Valid @RequestBody Food food){
        return foodService.createFood(restaurantId, food);
    }

    @GetMapping("/{restaurantId}/foods")
    public List<FoodResponse> getAllFoods(@PathVariable Integer restaurantId){
        return foodService.getAllFoods(restaurantId);
    }

    @GetMapping("/{foodId}")
    public FoodResponse getFoodById(@PathVariable Integer foodId){
        return foodService.getFoodById(foodId);
    }

    @PutMapping("/{foodId}")
    public FoodResponse updateFood(@PathVariable Integer foodId, @Valid @RequestBody Food updatedFood){
        return foodService.updateFood(foodId, updatedFood);
    }

    @DeleteMapping("/{foodId}")
    public void deleteFoodById(@PathVariable Integer foodId){
        foodService.deleteFoodById(foodId);
    }
}

