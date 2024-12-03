package com.example.prac.dto.data;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DishResponseDTO {
    private Long id; // ID блюда
    private String name; // Название блюда
    private String instructions; // Инструкции приготовления
    private List<IngredientDTO> ingredients; // Список ингредиентов
    private String imageUrl; // URL изображения
    private LocalDateTime dateTime;
}
