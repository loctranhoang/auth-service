package com.az.auth;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String PASSWORD_CREDENTIAL_TYPE = CredentialRepresentation.PASSWORD;

    private final KeycloakProperties keycloakProperties;

    public AuthService(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    public RegistrationResponse register(RegistrationRequest request) {
        validateRegistrationRequest(request);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);

        CredentialRepresentation password = new CredentialRepresentation();
        password.setType(PASSWORD_CREDENTIAL_TYPE);
        password.setValue(request.password());
        password.setTemporary(false);
        user.setCredentials(List.of(password));

        try (Keycloak keycloak = adminClient()) {
            RealmResource realm = keycloak.realm(keycloakProperties.getRealm());
            UsersResource users = realm.users();

            try (Response response = users.create(user)) {
                if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                    String userId = CreatedResponseUtil.getCreatedId(response);
                    return new RegistrationResponse(userId, request.username(), request.email());
                }

                if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                    throw new AuthException(
                            HttpStatus.CONFLICT,
                            "USER_ALREADY_EXISTS",
                            "A user with the supplied username or email already exists."
                    );
                }

                throw new AuthException(
                        HttpStatus.BAD_GATEWAY,
                        "KEYCLOAK_USER_CREATION_FAILED",
                        "Keycloak user creation failed with status " + response.getStatus() + "."
                );
            }
        } catch (AuthException exception) {
            throw exception;
        } catch (NotAuthorizedException exception) {
            throw new AuthException(
                    HttpStatus.BAD_GATEWAY,
                    "KEYCLOAK_ADMIN_AUTHENTICATION_FAILED",
                    "Keycloak rejected the configured admin client credentials.",
                    exception
            );
        } catch (ProcessingException exception) {
            throw new AuthException(
                    HttpStatus.BAD_GATEWAY,
                    "KEYCLOAK_UNAVAILABLE",
                    "Keycloak could not be reached.",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new AuthException(
                    HttpStatus.BAD_GATEWAY,
                    "KEYCLOAK_USER_CREATION_FAILED",
                    "Keycloak user creation failed.",
                    exception
            );
        }
    }

    public TokenResponse login(LoginRequest request) {
        validateLoginRequest(request);

        try (Keycloak keycloak = loginClient(request)) {
            return toTokenResponse(keycloak.tokenManager().getAccessToken());
        } catch (NotAuthorizedException | BadRequestException exception) {
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS",
                    "Invalid username or password.",
                    exception
            );
        } catch (ProcessingException exception) {
            throw new AuthException(
                    HttpStatus.BAD_GATEWAY,
                    "KEYCLOAK_UNAVAILABLE",
                    "Keycloak could not be reached.",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new AuthException(
                    HttpStatus.BAD_GATEWAY,
                    "KEYCLOAK_AUTHENTICATION_FAILED",
                    "Keycloak authentication failed.",
                    exception
            );
        }
    }

    private Keycloak adminClient() {
        String adminClientSecret = keycloakProperties.getAdminClientSecret();
        if (!hasText(adminClientSecret)) {
            throw new AuthException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "KEYCLOAK_ADMIN_CLIENT_SECRET_MISSING",
                    "Keycloak admin client secret is required to create users."
            );
        }

        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(keycloakProperties.getAdminRealm())
                .clientId(keycloakProperties.getAdminClientId())
                .clientSecret(adminClientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    private Keycloak loginClient(LoginRequest request) {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(keycloakProperties.getRealm())
                .clientId(keycloakProperties.getClientId())
                .username(request.username())
                .password(request.password())
                .grantType(OAuth2Constants.PASSWORD);

        if (hasText(keycloakProperties.getClientSecret())) {
            builder.clientSecret(keycloakProperties.getClientSecret());
        }

        return builder.build();
    }

    private TokenResponse toTokenResponse(AccessTokenResponse token) {
        return new TokenResponse(
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
    }

    private void validateRegistrationRequest(RegistrationRequest request) {
        if (request == null
                || !hasText(request.username())
                || !hasText(request.email())
                || !hasText(request.password())) {
            throw new AuthException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_REGISTRATION_REQUEST",
                    "Username, email, and password are required."
            );
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null || !hasText(request.username()) || !hasText(request.password())) {
            throw new AuthException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_LOGIN_REQUEST",
                    "Username and password are required."
            );
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static class AuthException extends RuntimeException {

        private final HttpStatus status;
        private final String code;

        AuthException(HttpStatus status, String code, String message) {
            super(message);
            this.status = status;
            this.code = code;
        }

        AuthException(HttpStatus status, String code, String message, Throwable cause) {
            super(message, cause);
            this.status = status;
            this.code = code;
        }

        public HttpStatus status() {
            return status;
        }

        public String code() {
            return code;
        }
    }
}

@Component
@ConfigurationProperties(prefix = "auth.keycloak")
class KeycloakProperties {

    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String adminRealm;
    private String adminClientId;
    private String adminClientSecret;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAdminRealm() {
        return adminRealm;
    }

    public void setAdminRealm(String adminRealm) {
        this.adminRealm = adminRealm;
    }

    public String getAdminClientId() {
        return adminClientId;
    }

    public void setAdminClientId(String adminClientId) {
        this.adminClientId = adminClientId;
    }

    public String getAdminClientSecret() {
        return adminClientSecret;
    }

    public void setAdminClientSecret(String adminClientSecret) {
        this.adminClientSecret = adminClientSecret;
    }
}
