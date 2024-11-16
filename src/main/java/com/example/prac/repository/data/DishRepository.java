package com.example.prac.repository.data;

import com.example.prac.model.data.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {
}
