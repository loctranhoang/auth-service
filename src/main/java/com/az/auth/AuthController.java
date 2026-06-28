package com.az.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        AuthService.RegisteredUser user = authService.register(new AuthService.RegistrationCommand(
                request.username(),
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(user.id(), user.username(), user.email()));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        AuthService.TokenResult token = authService.login(new AuthService.LoginCommand(
                request.username(),
                request.password()
        ));

        return new TokenResponse(
                token.accessToken(),
                token.expiresIn(),
                token.refreshExpiresIn(),
                token.refreshToken(),
                token.tokenType(),
                token.idToken(),
                token.notBeforePolicy(),
                token.sessionState(),
                token.scope()
        );
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

    public record RegistrationResponse(
            String id,
            String username,
            String email
    ) {
    }

    public record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("refresh_expires_in") Long refreshExpiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("id_token") String idToken,
            @JsonProperty("not-before-policy") Integer notBeforePolicy,
            @JsonProperty("session_state") String sessionState,
            String scope
    ) {
    }
}
