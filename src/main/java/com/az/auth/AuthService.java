package com.az.auth;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AuthService {

    private final KeycloakProperties properties;
    private final RestClient restClient;

    public AuthService(KeycloakProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    public RegistrationResponse register(RegistrationRequest request) {
        validateRegistration(request);
        String adminAccessToken = adminAccessToken();

        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri(adminUsersUri())
                    .headers(headers -> headers.setBearerAuth(adminAccessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(keycloakUserPayload(request))
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode() == HttpStatus.CREATED) {
                return new RegistrationResponse(userIdFromLocation(response.getHeaders().getLocation()), request.username(), "created");
            }

            throw new AuthServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "KEYCLOAK_USER_CREATE_FAILED",
                    "Keycloak user creation failed with status " + response.getStatusCode().value());
        } catch (AuthServiceException exception) {
            throw exception;
        } catch (HttpClientErrorException.Conflict exception) {
            throw new AuthServiceException(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", "User already exists", exception);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden exception) {
            throw new AuthServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "KEYCLOAK_ADMIN_AUTH_FAILED",
                    "Unable to authenticate Keycloak admin client",
                    exception);
        } catch (RestClientException exception) {
            throw new AuthServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "KEYCLOAK_USER_CREATE_FAILED",
                    "Unable to create user in Keycloak",
                    exception);
        }
    }

    public Map<String, Object> login(LoginRequest request) {
        validateLogin(request);

        try {
            return requestToken(passwordGrantForm(properties.getClientId(), properties.getClientSecret(), request.username(), request.password()));
        } catch (HttpClientErrorException.Unauthorized exception) {
            throw new AuthServiceException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS",
                    "Invalid username or password",
                    exception);
        } catch (RestClientException exception) {
            throw new AuthServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "KEYCLOAK_LOGIN_FAILED",
                    "Unable to authenticate against Keycloak",
                    exception);
        }
    }

    private String adminAccessToken() {
        if (StringUtils.hasText(properties.getAdminUsername()) && StringUtils.hasText(properties.getAdminPassword())) {
            Map<String, Object> token = requestToken(passwordGrantForm(
                    properties.getAdminClientId(),
                    properties.getAdminClientSecret(),
                    properties.getAdminUsername(),
                    properties.getAdminPassword()));
            return accessToken(token);
        }

        if (StringUtils.hasText(properties.getAdminClientSecret())) {
            Map<String, Object> token = requestToken(clientCredentialsForm(
                    properties.getAdminClientId(),
                    properties.getAdminClientSecret()));
            return accessToken(token);
        }

        throw new AuthServiceException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "KEYCLOAK_ADMIN_CREDENTIALS_MISSING",
                "Keycloak admin credentials are not configured");
    }

    private Map<String, Object> requestToken(MultiValueMap<String, String> form) {
        return restClient.post()
                .uri(tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    private MultiValueMap<String, String> passwordGrantForm(String clientId, String clientSecret, String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", requireConfig(clientId, "KEYCLOAK_CLIENT_ID", "Keycloak client id is not configured"));
        addIfPresent(form, "client_secret", clientSecret);
        form.add("username", username);
        form.add("password", password);
        return form;
    }

    private MultiValueMap<String, String> clientCredentialsForm(String clientId, String clientSecret) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", requireConfig(clientId, "KEYCLOAK_ADMIN_CLIENT_ID", "Keycloak admin client id is not configured"));
        form.add("client_secret", clientSecret);
        return form;
    }

    private Map<String, Object> keycloakUserPayload(RegistrationRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", request.username());
        payload.put("email", request.email());
        addIfPresent(payload, "firstName", request.firstName());
        addIfPresent(payload, "lastName", request.lastName());
        payload.put("enabled", true);
        payload.put("credentials", List.of(Map.of(
                "type", "password",
                "value", request.password(),
                "temporary", false)));
        return payload;
    }

    private URI tokenUri() {
        return UriComponentsBuilder.fromUriString(requireConfig(properties.getServerUrl(), "KEYCLOAK_SERVER_URL", "Keycloak server URL is not configured"))
                .pathSegment("realms", requireConfig(properties.getRealm(), "KEYCLOAK_REALM", "Keycloak realm is not configured"))
                .pathSegment("protocol", "openid-connect", "token")
                .build()
                .toUri();
    }

    private URI adminUsersUri() {
        return UriComponentsBuilder.fromUriString(requireConfig(properties.getServerUrl(), "KEYCLOAK_SERVER_URL", "Keycloak server URL is not configured"))
                .pathSegment("admin", "realms", requireConfig(properties.getRealm(), "KEYCLOAK_REALM", "Keycloak realm is not configured"), "users")
                .build()
                .toUri();
    }

    private String accessToken(Map<String, Object> token) {
        Object accessToken = token == null ? null : token.get("access_token");
        if (accessToken instanceof String value && StringUtils.hasText(value)) {
            return value;
        }
        throw new AuthServiceException(
                HttpStatus.BAD_GATEWAY,
                "KEYCLOAK_TOKEN_RESPONSE_INVALID",
                "Keycloak token response did not include an access token");
    }

    private String userIdFromLocation(URI location) {
        if (location == null || !StringUtils.hasText(location.getPath())) {
            return "";
        }

        String path = location.getPath();
        int lastSeparator = path.lastIndexOf('/');
        return lastSeparator >= 0 ? path.substring(lastSeparator + 1) : path;
    }

    private void validateRegistration(RegistrationRequest request) {
        if (request == null
                || !StringUtils.hasText(request.username())
                || !StringUtils.hasText(request.email())
                || !request.email().contains("@")
                || !StringUtils.hasText(request.password())
                || request.password().length() < 8) {
            throw new AuthServiceException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_REGISTRATION_REQUEST",
                    "Registration requires username, valid email, and password with at least 8 characters");
        }
    }

    private void validateLogin(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.username()) || !StringUtils.hasText(request.password())) {
            throw new AuthServiceException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_LOGIN_REQUEST",
                    "Login requires username and password");
        }
    }

    private String requireConfig(String value, String name, String message) {
        if (StringUtils.hasText(value)) {
            return value;
        }
        throw new AuthServiceException(HttpStatus.INTERNAL_SERVER_ERROR, name + "_MISSING", message);
    }

    private void addIfPresent(MultiValueMap<String, String> form, String name, String value) {
        if (StringUtils.hasText(value)) {
            form.add(name, value);
        }
    }

    private void addIfPresent(Map<String, Object> payload, String name, String value) {
        if (StringUtils.hasText(value)) {
            payload.put(name, value);
        }
    }

    public static class AuthServiceException extends RuntimeException {
        private final HttpStatus status;
        private final String code;

        AuthServiceException(HttpStatus status, String code, String message) {
            super(message);
            this.status = status;
            this.code = code;
        }

        AuthServiceException(HttpStatus status, String code, String message, Throwable cause) {
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
    private String adminClientId;
    private String adminClientSecret;
    private String adminUsername;
    private String adminPassword;

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

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
