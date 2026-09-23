package com.harsha.food_delivery.repository;

import com.harsha.food_delivery.model.Order;
import com.harsha.food_delivery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Integer> {
    List<Order> findByUser(User user);

    Optional<Order> findByIdAndUser(Integer id, User user);
}
