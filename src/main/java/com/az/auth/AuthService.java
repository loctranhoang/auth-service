package com.az.auth;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final KeycloakProperties keycloakProperties;

    public AuthService(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    public AuthController.RegistrationResponse register(AuthController.RegistrationRequest request) {
        keycloakProperties.requireAdminClientConfiguration();

        try (Keycloak keycloak = buildAdminClient()) {
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

            try (Response response = keycloak.realm(keycloakProperties.getRealm()).users().create(user)) {
                int status = response.getStatus();
                if (status == HttpStatusCodes.CREATED) {
                    return new AuthController.RegistrationResponse(
                            CreatedResponseUtil.getCreatedId(response),
                            request.username(),
                            request.email()
                    );
                }
                if (status == HttpStatusCodes.CONFLICT) {
                    throw new UserAlreadyExistsException("A Keycloak user already exists for the supplied username or email");
                }
                throw new KeycloakIntegrationException("Keycloak user creation failed with status " + status);
            }
        } catch (UserAlreadyExistsException | KeycloakConfigurationException exception) {
            throw exception;
        } catch (WebApplicationException | ProcessingException exception) {
            throw new KeycloakIntegrationException("Keycloak user creation request failed", exception);
        }
    }

    public AuthController.TokenResponse login(AuthController.LoginRequest request) {
        keycloakProperties.requireLoginClientConfiguration();

        try (Keycloak keycloak = buildLoginClient(request.username(), request.password())) {
            return toTokenResponse(keycloak.tokenManager().getAccessToken());
        } catch (NotAuthorizedException | BadRequestException exception) {
            throw new AuthenticationFailedException("Invalid username or password", exception);
        } catch (WebApplicationException | ProcessingException exception) {
            throw new KeycloakIntegrationException("Keycloak authentication request failed", exception);
        }
    }

    private Keycloak buildAdminClient() {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(keycloakProperties.getRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(keycloakProperties.getAdminClientId());
        applyClientSecret(builder, keycloakProperties.getAdminClientSecret());
        return builder.build();
    }

    private Keycloak buildLoginClient(String username, String password) {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(keycloakProperties.getRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(keycloakProperties.getClientId())
                .username(username)
                .password(password);
        applyClientSecret(builder, keycloakProperties.getClientSecret());
        return builder.build();
    }

    private static void applyClientSecret(KeycloakBuilder builder, String clientSecret) {
        if (hasText(clientSecret)) {
            builder.clientSecret(clientSecret);
        }
    }

    private static AuthController.TokenResponse toTokenResponse(AccessTokenResponse token) {
        return new AuthController.TokenResponse(
                token.getToken(),
                token.getExpiresIn(),
                token.getRefreshExpiresIn(),
                token.getRefreshToken(),
                token.getTokenType(),
                token.getNotBeforePolicy(),
                token.getSessionState(),
                token.getScope()
        );
    }

    private static String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static final class HttpStatusCodes {
        private static final int CREATED = 201;
        private static final int CONFLICT = 409;
    }
}

@Component
@ConfigurationProperties(prefix = "auth.keycloak")
class KeycloakProperties {

    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String adminClientId;
    private String adminClientSecret;

    String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    String getAdminClientId() {
        return adminClientId;
    }

    public void setAdminClientId(String adminClientId) {
        this.adminClientId = adminClientId;
    }

    String getAdminClientSecret() {
        return adminClientSecret;
    }

    public void setAdminClientSecret(String adminClientSecret) {
        this.adminClientSecret = adminClientSecret;
    }

    void requireLoginClientConfiguration() {
        requireConfigured("server-url", serverUrl);
        requireConfigured("realm", realm);
        requireConfigured("client-id", clientId);
    }

    void requireAdminClientConfiguration() {
        requireConfigured("server-url", serverUrl);
        requireConfigured("realm", realm);
        requireConfigured("admin-client-id", adminClientId);
        requireConfigured("admin-client-secret", adminClientSecret);
    }

    private static void requireConfigured(String propertyName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new KeycloakConfigurationException("Missing required auth.keycloak." + propertyName + " configuration");
        }
    }
}

class AuthenticationFailedException extends RuntimeException {

    AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

class UserAlreadyExistsException extends RuntimeException {

    UserAlreadyExistsException(String message) {
        super(message);
    }
}

class KeycloakConfigurationException extends RuntimeException {

    KeycloakConfigurationException(String message) {
        super(message);
    }
}

class KeycloakIntegrationException extends RuntimeException {

    KeycloakIntegrationException(String message) {
        super(message);
    }

    KeycloakIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
