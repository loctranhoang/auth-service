package com.az.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        AuthService.RegistrationResult result = authService.register(request.toCommand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(result.userId(), result.username(), result.email()));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        AuthService.TokenResult token = authService.login(request.toCommand());

        return new TokenResponse(
                token.tokenType(),
                token.accessToken(),
                token.expiresIn(),
                token.refreshExpiresIn(),
                token.refreshToken(),
                token.idToken(),
                token.scope(),
                token.sessionState()
        );
    }

    @ExceptionHandler(AuthService.DuplicateUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateUser(AuthService.DuplicateUserException exception) {
        return new ErrorResponse("USER_ALREADY_EXISTS", exception.getMessage());
    }

    @ExceptionHandler(AuthService.InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(AuthService.InvalidCredentialsException exception) {
        return new ErrorResponse("INVALID_CREDENTIALS", exception.getMessage());
    }

    @ExceptionHandler(AuthService.KeycloakConfigurationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleConfiguration(AuthService.KeycloakConfigurationException exception) {
        return new ErrorResponse("KEYCLOAK_CONFIGURATION_ERROR", exception.getMessage());
    }

    @ExceptionHandler(AuthService.KeycloakIntegrationException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleKeycloakIntegration(AuthService.KeycloakIntegrationException exception) {
        return new ErrorResponse("KEYCLOAK_INTEGRATION_ERROR", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request body was not valid.");

        return new ErrorResponse("INVALID_REQUEST", message);
    }

    public record RegistrationRequest(
            @NotBlank String username,
            @Email String email,
            String firstName,
            String lastName,
            @NotBlank String password
    ) {
        private AuthService.RegistrationCommand toCommand() {
            return new AuthService.RegistrationCommand(username, email, firstName, lastName, password);
        }
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
        private AuthService.LoginCommand toCommand() {
            return new AuthService.LoginCommand(username, password);
        }
    }

    public record RegistrationResponse(
            String userId,
            String username,
            String email
    ) {
    }

    public record TokenResponse(
            String tokenType,
            String accessToken,
            long expiresIn,
            long refreshExpiresIn,
            String refreshToken,
            String idToken,
            String scope,
            String sessionState
    ) {
    }

    public record ErrorResponse(
            String code,
            String message
    ) {
    }
}
