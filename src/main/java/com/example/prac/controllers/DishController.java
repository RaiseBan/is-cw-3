package com.example.prac.controllers;

import com.example.prac.dto.data.DishDTO;
import com.example.prac.dto.data.DishResponseDTO;
import com.example.prac.model.data.Dish;
import com.example.prac.service.calendar.DishService;
import com.example.prac.service.UserContextService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    private final DishService dishService;
    private final UserContextService userContextService;

    public DishController(DishService dishService, UserContextService userContextService) {
        this.dishService = dishService;
        this.userContextService = userContextService;
    }

    // Создание блюда
    @PostMapping
    public ResponseEntity<DishResponseDTO> createDish(@RequestBody DishDTO dishDTO) {
        Long userId = userContextService.getCurrentUserId(); // Получаем ID пользователя
        DishResponseDTO dish = dishService.createDish(dishDTO, userId);
        return new ResponseEntity<>(dish, HttpStatus.CREATED);
    }



    // Обновление блюда
    @PutMapping("/{dishId}")
    public ResponseEntity<DishResponseDTO> updateDish(@PathVariable Long dishId, @RequestBody DishDTO dishDTO) {
        DishResponseDTO updatedDish = dishService.updateDish(dishId, dishDTO);
        return ResponseEntity.ok(updatedDish);
    }

    // Удаление блюда
    @DeleteMapping("/{dishId}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long dishId) {
        dishService.deleteDish(dishId);
        return ResponseEntity.noContent().build();
    }

    // Получение всех блюд пользователя
    @GetMapping
    public ResponseEntity<List<DishResponseDTO>> getAllDishes() {
        Long userId = userContextService.getCurrentUserId(); // Получаем ID пользователя
        List<DishResponseDTO> dishes = dishService.getAllDishes(userId);
        return ResponseEntity.ok(dishes);
    }
}
