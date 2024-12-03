package com.example.prac.dto.data;

import lombok.Data;

@Data
public class ShoppingListDTO {
    private Long ingredientId; // ID ингредиента
    private String ingredientName; // Название ингредиента
    private String storeName;      // Название магазина
    private Double price;          // Цена
    private String dishName;       // Название блюда (может быть null)
    private Integer count;         // Количество
    private Boolean available;     // Доступность ингредиента
}
