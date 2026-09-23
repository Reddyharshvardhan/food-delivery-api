package com.harsha.food_delivery.Service;

import com.harsha.food_delivery.dto.RestaurantResponse;
import com.harsha.food_delivery.model.Restaurant;
import com.harsha.food_delivery.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository){
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public RestaurantResponse createRestaurant(Restaurant restaurant){
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return toRestaurantResponse(savedRestaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants(){
        List<Restaurant> restaurants = restaurantRepository.findAll();
        List<RestaurantResponse> responses = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            responses.add(toRestaurantResponse(restaurant));
        }
        return responses;
    }

    public RestaurantResponse toRestaurantResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getPhone()
        );
    }
}

