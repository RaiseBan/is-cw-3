package com.example.prac.controllers;

import com.example.prac.dto.data.IngredientDTO;
import com.example.prac.dto.data.IngredientDTONoId;
import com.example.prac.model.data.Ingredient;
import com.example.prac.repository.data.IngredientRepository;
import com.example.prac.service.IngredientService;
import com.example.prac.service.UserContextService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingredients")
@AllArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;
    private final UserContextService userContextService;
    private final IngredientRepository ingredientRepository;


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
