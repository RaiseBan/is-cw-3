package com.example.prac.repository.data;

import com.example.prac.model.data.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
//    Optional<Dish> getDishByUserId(Long userId);
    Optional<Dish> getDishByDishId(Long dishId);
}
