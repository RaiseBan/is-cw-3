package com.example.prac.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class IngredientService {

    private final EntityManager entityManager;

    public IngredientService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // Обновление доступности ингредиента
    public void updateIngredientAvailability(Long userId, Long ingredientId, boolean available) {

        entityManager.createNativeQuery("SELECT update_ingredient_availability(:userId, :ingredientId, :available)")
                .setParameter("userId", userId)
                .setParameter("ingredientId", ingredientId)
                .setParameter("available", available)
                .getSingleResult();
    }
}
