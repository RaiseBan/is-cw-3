package com.example.prac.controllers;

import com.example.prac.dto.data.*;
import com.example.prac.errorHandler.ResourceNotFoundException;
import com.example.prac.model.data.CalendarDish;
import com.example.prac.repository.data.CalendarDishRepository;
import com.example.prac.service.calendar.CalendarService;
import com.example.prac.service.UserContextService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@AllArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final UserContextService userContextService;
    private final CalendarDishRepository calendarDishRepository;



    // Добавление блюда в календарь
    @PostMapping("/add-dish")
    public ResponseEntity<Void> addDishToCalendar(@RequestBody CalendarDishDTO calendarDishDTO) {
        Long userId = userContextService.getCurrentUserId(); // Получаем ID пользователя
        calendarService.addDishToCalendar(userId, calendarDishDTO.getDishId(), calendarDishDTO.getCalendarDate());
        return ResponseEntity.ok().build();
    }

    // Удаление блюда из календаря
    @DeleteMapping("/remove-dish/{dishId}")
    public ResponseEntity<Void> removeDishFromCalendar(@PathVariable Long dishId) {
        Long userId = userContextService.getCurrentUserId(); // Получаем ID пользователя
        calendarService.removeDishFromCalendar(userId, dishId);
        return ResponseEntity.noContent().build();
    }

    // Получение всех блюд из календаря
    @GetMapping("/dishes")
    public ResponseEntity<List<DishResponseDTO>> getDishesFromCalendar() {
        Long userId = userContextService.getCurrentUserId(); // Получаем ID пользователя
        List<DishResponseDTO> dishes = calendarService.getDishesFromCalendar(userId);
        return ResponseEntity.ok(dishes);
    }
    @PutMapping("/dish/{dishId}")
    public ResponseEntity<CalendarDishDTOResponse> updateDishInCalendar(@PathVariable Long dishId, @RequestBody CalendarDishDTOResponse calendarDishDTOResponse) {
        CalendarDishDTOResponse updated = calendarService.updateDishInCalendar(dishId, calendarDishDTOResponse);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/shopping-list")
    public ResponseEntity<List<ShoppingListDTO>> getShoppingList(
            @RequestParam(required = false, defaultValue = "price") String sortBy,
            @RequestParam(required = false, defaultValue = "false") boolean groupByDish,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder) {
        Long userId = userContextService.getCurrentUserId();
        List<ShoppingListDTO> shoppingList = calendarService.getShoppingList(userId, sortBy, groupByDish, sortOrder);
        return ResponseEntity.ok(shoppingList);
    }

    @GetMapping("/original-dish/{calendarDishId}")
    public ResponseEntity<Long> getOriginalDishId(@PathVariable Long calendarDishId) {
        // Найти запись календарного блюда
        CalendarDish calendarDish = calendarDishRepository.findById(calendarDishId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar dish not found with id: " + calendarDishId));

        // Получить ID оригинального блюда
        Long originalDishId = calendarDish.getOriginalDish().getDishId();

        return ResponseEntity.ok(originalDishId);
    }
}
