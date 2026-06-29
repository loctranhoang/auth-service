package com.az.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @ExceptionHandler(AuthService.AuthException.class)
    public ResponseEntity<AuthErrorResponse> handleAuthException(AuthService.AuthException exception) {
        AuthErrorResponse response = new AuthErrorResponse(exception.code(), exception.getMessage());
        return ResponseEntity.status(exception.status()).body(response);
    }
}

record RegistrationRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        String password
) {
}

record LoginRequest(
        String username,
        String password
) {
}

record RegistrationResponse(
        String userId,
        String username,
        String email
) {
}

record TokenResponse(
        String accessToken,
        long expiresIn,
        long refreshExpiresIn,
        String refreshToken,
        String tokenType,
        String idToken,
        int notBeforePolicy,
        String sessionState,
        String scope
) {
}

record AuthErrorResponse(
        String code,
        String message
) {
}
