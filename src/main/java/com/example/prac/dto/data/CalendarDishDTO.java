package com.example.prac.dto.data;

import lombok.Data;

@Data
public class CalendarDishDTO {
    private Long dishId; // ID блюда
    private String calendarDate; // Дата и время добавления блюда в календарь
}
