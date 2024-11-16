package com.example.prac.service;

import com.example.prac.dto.auth.AuthenticationRequest;
import com.example.prac.dto.auth.AuthenticationResponse;
import com.example.prac.dto.auth.RegisterRequest;
//import com.example.prac.errorHandler.UserAlreadyExistsException;
import com.example.prac.model.authEntity.Role;
import com.example.prac.model.authEntity.User;
import com.example.prac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getUsername()).isPresent()) {
            throw new RuntimeException("A user with the same username already exists");
        }

        var user = User.builder()
                .email(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);


    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getUsername()).orElseThrow(() -> new UsernameNotFoundException("user not found"));
        var token = jwtService.generateToken(user);
        Role role = user.getRole();
        return AuthenticationResponse.builder()
                .token(token)
                .role(role)
                .build();
    }
}
