package com.az.auth;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import org.keycloak.OAuth2Constants;
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
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret:}") String clientSecret,
            @Value("${keycloak.admin.realm:master}") String adminRealm,
            @Value("${keycloak.admin.client-id:admin-cli}") String adminClientId,
            @Value("${keycloak.admin.client-secret:}") String adminClientSecret,
            @Value("${keycloak.admin.username}") String adminUsername,
            @Value("${keycloak.admin.password}") String adminPassword
    ) {
        this.keycloakSettings = new KeycloakSettings(
                serverUrl,
                realm,
                clientId,
                clientSecret,
                adminRealm,
                adminClientId,
                adminClientSecret,
                adminUsername,
                adminPassword
        );
    }

    public RegisteredUser register(RegistrationCommand command) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(command.username());
        user.setEmail(command.email());
        user.setFirstName(command.firstName());
        user.setLastName(command.lastName());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(command.password());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        try (Keycloak keycloak = adminClient();
             Response response = keycloak.realm(keycloakSettings.realm()).users().create(user)) {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                return new RegisteredUser(createdResourceId(response.getLocation()), command.username(), command.email());
            }

            if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists in Keycloak");
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Keycloak user creation failed with status " + response.getStatus()
            );
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (ProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak is not reachable", ex);
        }
    }

    public TokenResult login(LoginCommand command) {
        try (Keycloak keycloak = userClient(command.username(), command.password())) {
            AccessTokenResponse token = keycloak.tokenManager().getAccessToken();
            return new TokenResult(
                    token.getToken(),
                    token.getExpiresIn(),
                    token.getRefreshExpiresIn(),
                    token.getRefreshToken(),
                    token.getTokenType(),
                    token.getIdToken(),
                    token.getNotBeforePolicy(),
                    token.getSessionState(),
                    token.getScope()
            );
        } catch (NotAuthorizedException | BadRequestException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password", ex);
        } catch (ProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak is not reachable", ex);
        }
    }

    private Keycloak adminClient() {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(keycloakSettings.serverUrl())
                .realm(keycloakSettings.adminRealm())
                .clientId(keycloakSettings.adminClientId())
                .username(keycloakSettings.adminUsername())
                .password(keycloakSettings.adminPassword())
                .grantType(OAuth2Constants.PASSWORD);

        if (!keycloakSettings.adminClientSecret().isBlank()) {
            builder.clientSecret(keycloakSettings.adminClientSecret());
        }

        return builder.build();
    }

    private Keycloak userClient(String username, String password) {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(keycloakSettings.serverUrl())
                .realm(keycloakSettings.realm())
                .clientId(keycloakSettings.clientId())
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD);

        if (!keycloakSettings.clientSecret().isBlank()) {
            builder.clientSecret(keycloakSettings.clientSecret());
        }

        return builder.build();
    }

    private static String createdResourceId(URI location) {
        if (location == null) {
            return null;
        }

        String path = location.getPath();
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
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
            String adminPassword
    ) {
    }

    public record RegistrationCommand(
            String username,
            String email,
            String password,
            String firstName,
            String lastName
    ) {
    }

    public record LoginCommand(
            String username,
            String password
    ) {
    }

    public record RegisteredUser(
            String id,
            String username,
            String email
    ) {
    }

    public record TokenResult(
            String accessToken,
            Long expiresIn,
            Long refreshExpiresIn,
            String refreshToken,
            String tokenType,
            String idToken,
            Integer notBeforePolicy,
            String sessionState,
            String scope
    ) {
    }
}
