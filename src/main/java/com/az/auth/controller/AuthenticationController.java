package com.az.auth.controller;

import com.az.auth.dto.AuthenticationTokenDto;
import com.az.auth.dto.UserLoginDto;
import com.az.auth.dto.UserRegistrationDto;
import com.az.auth.dto.UserRegistrationResponseDto;
import com.az.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDto> register(@Valid @RequestBody UserRegistrationDto request) {
        UserRegistrationResponseDto response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationTokenDto> login(@Valid @RequestBody UserLoginDto request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}
