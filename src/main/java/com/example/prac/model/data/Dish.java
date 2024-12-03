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

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String instructions;

    @Column(nullable = true) // Поле для URL картинки
    private String imageUrl;

    @ManyToMany
    @JoinTable(
            name = "dish_ingredient",
            joinColumns = @JoinColumn(name = "dish_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private List<Ingredient> ingredients;
}
