package com.az.auth.integration;

import com.az.auth.config.KeycloakProperties;
import com.az.auth.dto.UserLoginDto;
import com.az.auth.dto.UserRegistrationDto;
import com.az.auth.exception.AuthenticationFailedException;
import com.az.auth.exception.IdentityProviderException;
import com.az.auth.exception.UserAlreadyExistsException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
public class KeycloakIdentityProvider {

    private final KeycloakProperties properties;

    public KeycloakIdentityProvider(KeycloakProperties properties) {
        this.properties = properties;
    }

    public String createUser(UserRegistrationDto request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(request.password());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setCredentials(List.of(credential));

        try (Keycloak keycloak = adminClient()) {
            Response response = keycloak.realm(properties.getRealm()).users().create(user);
            try (response) {
                return handleCreateUserResponse(response, request.username());
            }
        } catch (UserAlreadyExistsException exception) {
            throw exception;
        } catch (ProcessingException | WebApplicationException exception) {
            throw new IdentityProviderException("Failed to create user in Keycloak", exception);
        }
    }

    public AccessTokenResponse authenticate(UserLoginDto request) {
        try (Keycloak keycloak = passwordGrantClient(request)) {
            return keycloak.tokenManager().getAccessToken();
        } catch (NotAuthorizedException exception) {
            throw new AuthenticationFailedException("Invalid username or password", exception);
        } catch (ProcessingException | WebApplicationException exception) {
            throw new IdentityProviderException("Failed to authenticate with Keycloak", exception);
        }
    }

    private String handleCreateUserResponse(Response response, String username) {
        int status = response.getStatus();
        if (status == Response.Status.CREATED.getStatusCode()) {
            return CreatedResponseUtil.getCreatedId(response);
        }
        if (status == Response.Status.CONFLICT.getStatusCode()) {
            throw new UserAlreadyExistsException("User already exists: " + username);
        }
        throw new IdentityProviderException("Keycloak user creation failed with status " + status);
    }

    private Keycloak adminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(properties.getServerUrl())
                .realm(properties.getRealm())
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    private Keycloak passwordGrantClient(UserLoginDto request) {
        return KeycloakBuilder.builder()
                .serverUrl(properties.getServerUrl())
                .realm(properties.getRealm())
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .username(request.username())
                .password(request.password())
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }
}
