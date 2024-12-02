package com.example.prac.dto.data;

import lombok.Data;

import java.util.List;

@Data
public class DishDTO {
    private String name; // Название блюда
    private String instructions; // Было description
    private List<IngredientDTO> ingredients; // Список ингредиентов
}
