package com.az.auth.dto;

import org.keycloak.representations.AccessTokenResponse;

public record AuthenticationTokenDto(
        String accessToken,
        long expiresIn,
        long refreshExpiresIn,
        String refreshToken,
        String tokenType,
        String idToken,
        int notBeforePolicy,
        String sessionState,
        String scope
) {
    public static AuthenticationTokenDto from(AccessTokenResponse response) {
        return new AuthenticationTokenDto(
                response.getToken(),
                response.getExpiresIn(),
                response.getRefreshExpiresIn(),
                response.getRefreshToken(),
                response.getTokenType(),
                response.getIdToken(),
                response.getNotBeforePolicy(),
                response.getSessionState(),
                response.getScope()
        );
    }
}
