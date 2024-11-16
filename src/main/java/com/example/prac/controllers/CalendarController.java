package com.example.prac.controllers;

import com.example.prac.dto.data.CalendarDishDTO;
import com.example.prac.dto.data.DishResponseDTO;
import com.example.prac.dto.data.ShoppingListDTO;
import com.example.prac.service.calendar.CalendarService;
import com.example.prac.service.UserContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final UserContextService userContextService;

    public CalendarController(CalendarService calendarService, UserContextService userContextService) {
        this.calendarService = calendarService;
        this.userContextService = userContextService;
    }

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


    @GetMapping("/shopping-list")
    public ResponseEntity<List<ShoppingListDTO>> getShoppingList(
            @RequestParam(required = false, defaultValue = "price") String sortBy,
            @RequestParam(required = false, defaultValue = "false") boolean groupByDish,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder) {
        Long userId = userContextService.getCurrentUserId();
        List<ShoppingListDTO> shoppingList = calendarService.getShoppingList(userId, sortBy, groupByDish, sortOrder);
        return ResponseEntity.ok(shoppingList);
    }
}
