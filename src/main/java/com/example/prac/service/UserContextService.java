package com.example.prac.service;

import com.example.prac.model.authEntity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    public Long getCurrentUserId() {
        // Здесь предполагается, что ваш пользователь хранит ID в UserDetails
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUserId(); // Или другой способ получения ID пользователя
    }

    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
