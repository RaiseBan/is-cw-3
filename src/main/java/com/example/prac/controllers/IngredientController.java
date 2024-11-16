package com.example.prac.controllers;

import com.example.prac.service.IngredientService;
import com.example.prac.service.UserContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;
    private final UserContextService userContextService;

    public IngredientController(IngredientService ingredientService, UserContextService userContextService) {
        this.ingredientService = ingredientService;
        this.userContextService = userContextService;
    }

    // Обновление доступности ингредиента
    @PutMapping("/{ingredientId}/availability")
    public ResponseEntity<Void> updateIngredientAvailability(
            @PathVariable Long ingredientId,
            @RequestParam boolean available) {
        Long userId = userContextService.getCurrentUserId();
        ingredientService.updateIngredientAvailability(userId, ingredientId, available);
        return ResponseEntity.ok().build();
    }
}
