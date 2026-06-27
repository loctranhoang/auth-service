package com.az.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.az.auth.controller.AuthenticationController;
import com.az.auth.dto.AuthenticationTokenDto;
import com.az.auth.dto.UserRegistrationResponseDto;
import com.az.auth.exception.AuthenticationFailedException;
import com.az.auth.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthenticationController.class)
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void registerCreatesUserInIdentityProvider() throws Exception {
        when(authenticationService.register(any()))
                .thenReturn(new UserRegistrationResponseDto("keycloak-user-id"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jane",
                                  "email": "jane@example.com",
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("keycloak-user-id"));
    }

    @Test
    void loginReturnsKeycloakTokenPayload() throws Exception {
        when(authenticationService.login(any()))
                .thenReturn(new AuthenticationTokenDto(
                        "access-token",
                        300,
                        1800,
                        "refresh-token",
                        "Bearer",
                        "id-token",
                        0,
                        "session-state",
                        "openid profile"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jane",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        when(authenticationService.login(any()))
                .thenThrow(new AuthenticationFailedException("Invalid username or password", null));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jane",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
