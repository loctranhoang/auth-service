package com.az.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final KeycloakSettings keycloakSettings;

    public AuthService(
            @Value("${auth.keycloak.server-url}") String serverUrl,
            @Value("${auth.keycloak.realm}") String realm,
            @Value("${auth.keycloak.client-id}") String clientId,
            @Value("${auth.keycloak.client-secret:}") String clientSecret,
            @Value("${auth.keycloak.admin-realm}") String adminRealm,
            @Value("${auth.keycloak.admin-client-id}") String adminClientId,
            @Value("${auth.keycloak.admin-client-secret:}") String adminClientSecret,
            @Value("${auth.keycloak.admin-username:}") String adminUsername,
            @Value("${auth.keycloak.admin-password:}") String adminPassword) {
        this.keycloakSettings = new KeycloakSettings(
                requireConfigured(serverUrl, "auth.keycloak.server-url"),
                requireConfigured(realm, "auth.keycloak.realm"),
                requireConfigured(clientId, "auth.keycloak.client-id"),
                clientSecret,
                requireConfigured(adminRealm, "auth.keycloak.admin-realm"),
                requireConfigured(adminClientId, "auth.keycloak.admin-client-id"),
                adminClientSecret,
                adminUsername,
                adminPassword);
    }

    public RegistrationResponse register(RegistrationRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(blankToNull(request.firstName()));
        user.setLastName(blankToNull(request.lastName()));
        user.setEnabled(true);

        CredentialRepresentation password = new CredentialRepresentation();
        password.setType(CredentialRepresentation.PASSWORD);
        password.setTemporary(false);
        password.setValue(request.password());
        user.setCredentials(List.of(password));

        try (Keycloak keycloak = adminKeycloakClient()) {
            try (Response response = keycloak.realm(keycloakSettings.realm()).users().create(user)) {
                if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                    return new RegistrationResponse(
                            CreatedResponseUtil.getCreatedId(response),
                            request.username(),
                            request.email(),
                            true);
                }

                if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
                }

                throw keycloakFailure("Failed to create user in Keycloak", response.getStatusInfo());
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (ProcessingException exception) {
            throw keycloakUnavailable(exception);
        } catch (WebApplicationException exception) {
            throw keycloakFailure("Failed to create user in Keycloak", exception);
        }
    }

    public AuthenticationResponse login(LoginRequest request) {
        try (Keycloak keycloak = userKeycloakClient(request)) {
            return AuthenticationResponse.from(keycloak.tokenManager().getAccessToken());
        } catch (NotAuthorizedException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password", exception);
        } catch (ProcessingException exception) {
            throw keycloakUnavailable(exception);
        } catch (WebApplicationException exception) {
            throw keycloakFailure("Failed to authenticate with Keycloak", exception);
        }
    }

    private Keycloak adminKeycloakClient() {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(keycloakSettings.serverUrl())
                .realm(keycloakSettings.adminRealm())
                .clientId(keycloakSettings.adminClientId());

        if (hasText(keycloakSettings.adminUsername()) && hasText(keycloakSettings.adminPassword())) {
            builder.grantType(OAuth2Constants.PASSWORD)
                    .username(keycloakSettings.adminUsername())
                    .password(keycloakSettings.adminPassword());
            if (hasText(keycloakSettings.adminClientSecret())) {
                builder.clientSecret(keycloakSettings.adminClientSecret());
            }
            return builder.build();
        }

        if (hasText(keycloakSettings.adminClientSecret())) {
            return builder.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientSecret(keycloakSettings.adminClientSecret())
                    .build();
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Keycloak admin credentials are not configured");
    }

    private Keycloak userKeycloakClient(LoginRequest request) {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(keycloakSettings.serverUrl())
                .realm(keycloakSettings.realm())
                .clientId(keycloakSettings.clientId())
                .grantType(OAuth2Constants.PASSWORD)
                .username(request.username())
                .password(request.password());

        if (hasText(keycloakSettings.clientSecret())) {
            builder.clientSecret(keycloakSettings.clientSecret());
        }

        return builder.build();
    }

    private static String requireConfigured(String value, String propertyName) {
        if (!hasText(value)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Missing required configuration: " + propertyName);
        }
        return value;
    }

    private static String blankToNull(String value) {
        return hasText(value) ? value : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static ResponseStatusException keycloakUnavailable(ProcessingException exception) {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Keycloak is unavailable",
                exception);
    }

    private static ResponseStatusException keycloakFailure(String message, Response.StatusType statusInfo) {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                message + " (" + statusInfo.getStatusCode() + " " + statusInfo.getReasonPhrase() + ")");
    }

    private static ResponseStatusException keycloakFailure(String message, WebApplicationException exception) {
        Response response = exception.getResponse();
        if (response == null) {
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message, exception);
        }

        return keycloakFailure(message, response.getStatusInfo());
    }

    private record KeycloakSettings(
            String serverUrl,
            String realm,
            String clientId,
            String clientSecret,
            String adminRealm,
            String adminClientId,
            String adminClientSecret,
            String adminUsername,
            String adminPassword) {
    }

    public record RegistrationRequest(
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank String password,
            String firstName,
            String lastName) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record RegistrationResponse(
            String id,
            String username,
            String email,
            boolean enabled) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AuthenticationResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn,
            @JsonProperty("refresh_expires_in") long refreshExpiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("id_token") String idToken,
            String scope) {

        private static AuthenticationResponse from(AccessTokenResponse response) {
            return new AuthenticationResponse(
                    response.getToken(),
                    response.getExpiresIn(),
                    response.getRefreshExpiresIn(),
                    response.getRefreshToken(),
                    response.getTokenType(),
                    response.getIdToken(),
                    response.getScope());
        }
    }
}
