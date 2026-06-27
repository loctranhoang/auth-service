package com.az.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.az.auth.dto.UserLoginDto;
import com.az.auth.dto.UserRegistrationDto;
import com.az.auth.service.AuthenticationService;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void registrationDelegatesToAuthenticationService() throws Exception {
        URI userLocation = URI.create("http://localhost:8080/admin/realms/auth/users/user-id");
        when(authenticationService.register(any(UserRegistrationDto.class))).thenReturn(userLocation);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "alice",
                                  "email": "alice@example.com",
                                  "password": "S3cret!",
                                  "firstName": "Alice",
                                  "lastName": "Anderson"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, userLocation.toString()));

        verify(authenticationService).register(any(UserRegistrationDto.class));
    }

    @Test
    void loginReturnsKeycloakTokenPayload() throws Exception {
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setToken("access-token");
        tokenResponse.setRefreshToken("refresh-token");
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(300);
        when(authenticationService.login(any(UserLoginDto.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "S3cret!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(300));

        verify(authenticationService).login(any(UserLoginDto.class));
    }

    @Test
    void invalidCredentialsReturnUnauthorized() throws Exception {
        when(authenticationService.login(any(UserLoginDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
