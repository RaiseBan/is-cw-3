package com.example.prac.repository.data;

import com.example.prac.model.data.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    Optional<Calendar> findByUser_UserId(Long userId);
}
