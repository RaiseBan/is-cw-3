package com.example.prac.repository.data;

import com.example.prac.model.data.CalendarDish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarDishRepository extends JpaRepository<CalendarDish, Long> {

}
