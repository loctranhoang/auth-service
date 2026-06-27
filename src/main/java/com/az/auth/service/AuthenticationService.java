package com.az.auth.service;

import com.az.auth.dto.UserLoginDto;
import com.az.auth.dto.UserRegistrationDto;
import jakarta.ws.rs.ClientErrorException;
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
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

    private final String serverUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final String adminClientId;
    private final String adminClientSecret;
    private final String adminUsername;
    private final String adminPassword;

    public AuthenticationService(
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret:}") String clientSecret,
            @Value("${keycloak.admin-client-id}") String adminClientId,
            @Value("${keycloak.admin-client-secret:}") String adminClientSecret,
            @Value("${keycloak.admin-username:}") String adminUsername,
            @Value("${keycloak.admin-password:}") String adminPassword
    ) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.adminClientId = adminClientId;
        this.adminClientSecret = adminClientSecret;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public URI register(UserRegistrationDto request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setCredentials(List.of(passwordCredential(request.password())));

        try (Keycloak keycloak = buildAdminClient();
                Response response = keycloak.realm(realm).users().create(user)) {
            int status = response.getStatus();
            if (status == Response.Status.CREATED.getStatusCode()) {
                return response.getLocation();
            }
            if (status == Response.Status.CONFLICT.getStatusCode()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists in Keycloak");
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Keycloak user registration failed with status " + status
            );
        }
    }

    public AccessTokenResponse login(UserLoginDto request) {
        try (Keycloak keycloak = buildPasswordGrantClient(request.username(), request.password())) {
            return keycloak.tokenManager().getAccessToken();
        } catch (ClientErrorException ex) {
            if (ex.getResponse() != null
                    && ex.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password", ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak authentication failed", ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak authentication failed", ex);
        }
    }

    private CredentialRepresentation passwordCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(password);
        return credential;
    }

    private Keycloak buildAdminClient() {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(adminClientId)
                .grantType(resolveAdminGrantType());

        if (StringUtils.hasText(adminClientSecret)) {
            builder.clientSecret(adminClientSecret);
        }
        if (StringUtils.hasText(adminUsername)) {
            builder.username(adminUsername);
        }
        if (StringUtils.hasText(adminPassword)) {
            builder.password(adminPassword);
        }

        return builder.build();
    }

    private Keycloak buildPasswordGrantClient(String username, String password) {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .grantType(OAuth2Constants.PASSWORD)
                .username(username)
                .password(password);

        if (StringUtils.hasText(clientSecret)) {
            builder.clientSecret(clientSecret);
        }

        return builder.build();
    }

    private String resolveAdminGrantType() {
        if (StringUtils.hasText(adminUsername) || StringUtils.hasText(adminPassword)) {
            return OAuth2Constants.PASSWORD;
        }
        return OAuth2Constants.CLIENT_CREDENTIALS;
    }
}
