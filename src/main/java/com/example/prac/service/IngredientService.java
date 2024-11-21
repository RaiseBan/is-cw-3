package com.example.prac.service;

import com.example.prac.model.data.Ingredient;
import com.example.prac.repository.data.AvailableIngredientsRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IngredientService {

    private final EntityManager entityManager;
    private final UserContextService userContextService;
    private AvailableIngredientsRepository availableIngredientsRepository;

    // Обновление доступности ингредиента
    public void updateIngredientAvailability(Long userId, Long ingredientId, boolean available) {
        boolean isIngredientOwnedByUser = availableIngredientsRepository.existsByUserAndIngredient(
                userContextService.getCurrentUser(),
                entityManager.find(Ingredient.class, ingredientId)
        );
        entityManager.createNativeQuery("SELECT update_ingredient_availability(CAST(:userId AS INTEGER), CAST(:ingredientId AS INTEGER), CAST(:available AS BOOLEAN))")
                .setParameter("userId", userId)
                .setParameter("ingredientId", ingredientId)
                .setParameter("available", available)
                .getSingleResult();

    }
}
