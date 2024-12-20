package com.example.prac.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IngredientDTO {
    private Long id;
    private String name; // Название ингредиента
    private String unit; // Единица измерения
}

