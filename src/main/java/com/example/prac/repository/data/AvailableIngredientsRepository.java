package com.example.prac.repository.data;

import com.example.prac.model.authEntity.User;
import com.example.prac.model.data.AvailableIngredients;
import com.example.prac.model.data.Ingredient;
import jakarta.persistence.MapKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface AvailableIngredientsRepository extends JpaRepository<AvailableIngredients, Long> {
    boolean existsByUserAndIngredient(User user, Ingredient ingredient);

    @Query("SELECT ai.ingredient.name AS name, ai.ingredient.ingredientId AS id " +
            "FROM AvailableIngredients ai " +
            "WHERE ai.user.userId = :userId AND ai.available = true")
    @MapKey(name = "name")
    Map<String, Long> findAvailableIngredientsByUserId(@Param("userId") Long userId);



}
