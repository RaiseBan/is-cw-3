package com.example.prac.model.data;

import com.example.prac.model.authEntity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AvailableIngredients {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availableIngredientId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false)
    private Boolean available = false;
}
