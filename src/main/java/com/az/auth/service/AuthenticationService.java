package com.az.auth.service;

import com.az.auth.dto.AuthenticationTokenDto;
import com.az.auth.dto.UserLoginDto;
import com.az.auth.dto.UserRegistrationDto;
import com.az.auth.dto.UserRegistrationResponseDto;
import com.az.auth.integration.KeycloakIdentityProvider;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final KeycloakIdentityProvider identityProvider;

    public AuthenticationService(KeycloakIdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public UserRegistrationResponseDto register(UserRegistrationDto request) {
        return new UserRegistrationResponseDto(identityProvider.createUser(request));
    }

    public AuthenticationTokenDto login(UserLoginDto request) {
        AccessTokenResponse tokenResponse = identityProvider.authenticate(request);
        return AuthenticationTokenDto.from(tokenResponse);
    }
}
