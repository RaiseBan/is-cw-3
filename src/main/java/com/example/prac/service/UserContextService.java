package com.example.prac.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    public Long getCurrentUserId() {
        // Здесь предполагается, что ваш пользователь хранит ID в UserDetails
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(userDetails.getUsername()); // Или другой способ получения ID пользователя
    }
}
