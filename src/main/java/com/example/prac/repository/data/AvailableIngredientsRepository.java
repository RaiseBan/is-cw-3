package com.example.prac.repository.data;

import com.example.prac.model.authEntity.User;
import com.example.prac.model.data.AvailableIngredients;
import com.example.prac.model.data.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvailableIngredientsRepository extends JpaRepository<AvailableIngredients, Long> {
    boolean existsByUserAndIngredient(User user, Ingredient ingredient);
}
