package com.example.prac.dto.auth;

import lombok.Data;

@Data
public class VerificationRequest {
    private String email;
    private String code;
}
