package com.az.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationFailure(AuthenticationFailedException exception) {
        return new ErrorResponse("INVALID_CREDENTIALS", exception.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserAlreadyExists(UserAlreadyExistsException exception) {
        return new ErrorResponse("USER_ALREADY_EXISTS", exception.getMessage());
    }

    @ExceptionHandler(KeycloakConfigurationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleConfigurationFailure(KeycloakConfigurationException exception) {
        return new ErrorResponse("KEYCLOAK_CONFIGURATION_ERROR", exception.getMessage());
    }

    @ExceptionHandler(KeycloakIntegrationException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleKeycloakFailure(KeycloakIntegrationException exception) {
        return new ErrorResponse("KEYCLOAK_INTEGRATION_ERROR", exception.getMessage());
    }

    public record RegistrationRequest(
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank String password,
            String firstName,
            String lastName
    ) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record RegistrationResponse(String userId, String username, String email) {
    }

    public record TokenResponse(
            String accessToken,
            Long expiresIn,
            Long refreshExpiresIn,
            String refreshToken,
            String tokenType,
            Integer notBeforePolicy,
            String sessionState,
            String scope
    ) {
    }

    public record ErrorResponse(String code, String message) {
    }
}
