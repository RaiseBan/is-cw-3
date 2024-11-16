package com.example.prac.controllers;

import com.example.prac.dto.auth.AuthenticationRequest;
import com.example.prac.dto.auth.AuthenticationResponse;
import com.example.prac.dto.auth.RegisterRequest;
import com.example.prac.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok("Пользователь `" + request.getUsername() +"` зарегистрирован. " );
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/verify-token")
    public ResponseEntity<?> checkToken() {
        return ResponseEntity.ok("Token is valid");
    }

}
