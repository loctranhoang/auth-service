package com.az.auth;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConfigurationProperties(prefix = "auth.keycloak")
public class AuthService {

    private String serverUrl = "http://localhost:8080";
    private String realm = "auth";
    private String clientId = "auth-service";
    private String clientSecret = "";
    private String adminRealm = "master";
    private String adminClientId = "auth-service-admin";
    private String adminClientSecret = "";
    private String adminUsername = "";
    private String adminPassword = "";

    public RegistrationResult register(RegistrationCommand command) {
        try (Keycloak keycloak = buildAdminClient()) {
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(command.username());
            user.setEmail(command.email());
            user.setFirstName(command.firstName());
            user.setLastName(command.lastName());
            user.setCredentials(List.of(passwordCredential(command.password())));

            try (Response response = keycloak.realm(realm).users().create(user)) {
                if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                    return new RegistrationResult(
                            CreatedResponseUtil.getCreatedId(response),
                            command.username(),
                            command.email()
                    );
                }

                if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                    throw new DuplicateUserException("A user with the supplied username or email already exists.");
                }

                throw new KeycloakIntegrationException(
                        "Keycloak rejected user creation with HTTP " + response.getStatus() + "."
                );
            }
        } catch (DuplicateUserException | KeycloakConfigurationException | KeycloakIntegrationException exception) {
            throw exception;
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException("Unable to reach Keycloak while creating the user.", exception);
        } catch (RuntimeException exception) {
            throw new KeycloakIntegrationException("Keycloak user creation failed.", exception);
        }
    }

    public TokenResult login(LoginCommand command) {
        try (Keycloak keycloak = buildLoginClient(command.username(), command.password())) {
            AccessTokenResponse token = keycloak.tokenManager().grantToken();

            return new TokenResult(
                    token.getTokenType(),
                    token.getToken(),
                    token.getExpiresIn(),
                    token.getRefreshExpiresIn(),
                    token.getRefreshToken(),
                    token.getIdToken(),
                    token.getScope(),
                    token.getSessionState()
            );
        } catch (NotAuthorizedException exception) {
            throw new InvalidCredentialsException("The supplied username or password was not accepted by Keycloak.");
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException("Unable to reach Keycloak while authenticating the user.", exception);
        } catch (RuntimeException exception) {
            throw new KeycloakIntegrationException("Keycloak authentication failed.", exception);
        }
    }

    private Keycloak buildAdminClient() {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(StringUtils.hasText(adminRealm) ? adminRealm : realm)
                .clientId(adminClientId);

        if (StringUtils.hasText(adminClientSecret)) {
            return builder.clientSecret(adminClientSecret)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .build();
        }

        if (StringUtils.hasText(adminUsername) && StringUtils.hasText(adminPassword)) {
            return builder.username(adminUsername)
                    .password(adminPassword)
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();
        }

        throw new KeycloakConfigurationException(
                "Keycloak admin credentials are required for registration. Configure an admin client secret or admin username and password."
        );
    }

    private Keycloak buildLoginClient(String username, String password) {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD);

        if (StringUtils.hasText(clientSecret)) {
            builder.clientSecret(clientSecret);
        }

        return builder.build();
    }

    private CredentialRepresentation passwordCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(password);

        return credential;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setAdminRealm(String adminRealm) {
        this.adminRealm = adminRealm;
    }

    public void setAdminClientId(String adminClientId) {
        this.adminClientId = adminClientId;
    }

    public void setAdminClientSecret(String adminClientSecret) {
        this.adminClientSecret = adminClientSecret;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public record RegistrationCommand(
            String username,
            String email,
            String firstName,
            String lastName,
            String password
    ) {
    }

    public record LoginCommand(
            String username,
            String password
    ) {
    }

    public record RegistrationResult(
            String userId,
            String username,
            String email
    ) {
    }

    public record TokenResult(
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

    public static class DuplicateUserException extends RuntimeException {
        public DuplicateUserException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class KeycloakConfigurationException extends RuntimeException {
        public KeycloakConfigurationException(String message) {
            super(message);
        }
    }

    public static class KeycloakIntegrationException extends RuntimeException {
        public KeycloakIntegrationException(String message) {
            super(message);
        }

        public KeycloakIntegrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
