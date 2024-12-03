package com.example.prac.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDishDTOResponse { 
    private Long id;          // ID записи в календаре
    private Long dishId;      // ID оригинального блюда
    private String time;      // Новое время
}
