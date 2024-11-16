package com.example.prac.model.data;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CalendarDish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(nullable = false)
    private LocalDateTime time; // Время добавления блюда в календарь
}
