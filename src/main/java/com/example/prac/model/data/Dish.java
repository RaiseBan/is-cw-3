package com.example.prac.model.data;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dishId;

    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false, length = 40)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String instructions;

//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "recipe_id", nullable = false, unique = true)
//    private Recipe recipe;

    @ManyToMany
    @JoinTable(
            name = "dish_ingredient", // Имя промежуточной таблицы
            joinColumns = @JoinColumn(name = "dish_id"), // Колонка для блюда
            inverseJoinColumns = @JoinColumn(name = "ingredient_id") // Колонка для ингредиента
    )
    private List<Ingredient> ingredients;
}
