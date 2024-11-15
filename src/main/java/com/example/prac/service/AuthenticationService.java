package com.example.prac.service;

import com.example.prac.dto.auth.AuthenticationRequest;
import com.example.prac.dto.auth.AuthenticationResponse;
import com.example.prac.dto.auth.RegisterRequest;
import com.example.prac.dto.auth.VerificationRequest;
import com.example.prac.model.authEntity.Role;
import com.example.prac.model.authEntity.User;
import com.example.prac.model.authEntity.VerificationToken;
import com.example.prac.repository.UserRepository;
import com.example.prac.repository.VerificationTokenRepository;
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
    private final VerificationTokenRepository tokenRepository; // репозиторий для работы с токенами
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public void register(RegisterRequest request) {


        var user = User.builder()
                .email(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isVerified(false)
                .build();

        userRepository.save(user);

        // Создаем и сохраняем токен подтверждения
        VerificationToken verificationToken = createVerificationToken(user);
        tokenRepository.save(verificationToken);

    }

    private VerificationToken createVerificationToken(User user) {
        String token = generateVerificationCode();
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(15); // код будет действителен 15 минут

        return VerificationToken.builder()
                .token(token)
                .expirationDate(expirationDate)
                .user(user)
                .used(false)
                .build();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // шестизначный код
        return String.valueOf(code);
    }



    public AuthenticationResponse verifyCode(VerificationRequest verificationRequest) {
        String email = verificationRequest.getEmail();
        String code = verificationRequest.getCode();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        VerificationToken verificationToken = tokenRepository.findByUserAndTokenAndUsedFalse(user, code)
                .orElseThrow(() -> new IllegalArgumentException("Неверный или уже использованный код подтверждения"));

        if (verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Код подтверждения истек");
        }

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        user.setVerified(true);
        userRepository.save(user);

        var token = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .role(user.getRole())
                .build();
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
