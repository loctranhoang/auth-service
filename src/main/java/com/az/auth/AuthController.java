package com.az.auth;

import java.net.URI;
import java.util.Map;
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
        RegistrationResponse response = authService.register(request);
        String userReference = response.userId() == null || response.userId().isBlank()
                ? response.username()
                : response.userId();
        return ResponseEntity.created(URI.create("/auth/users/" + userReference)).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @ExceptionHandler(AuthService.AuthServiceException.class)
    public ResponseEntity<AuthErrorResponse> handleAuthServiceException(AuthService.AuthServiceException exception) {
        return ResponseEntity
                .status(exception.status())
                .body(new AuthErrorResponse(exception.code(), exception.getMessage()));
    }
}

record RegistrationRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        String password) {
}

record LoginRequest(
        String username,
        String password) {
}

record RegistrationResponse(String userId, String username, String status) {
}

record AuthErrorResponse(String code, String message) {
}
