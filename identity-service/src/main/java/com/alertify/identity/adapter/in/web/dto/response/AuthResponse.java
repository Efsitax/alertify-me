package com.alertify.identity.adapter.in.web.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user,
        long expiresIn
) {
    public static AuthResponse of(String token, UserResponse userResponse, long expirationMillis) {
        return new AuthResponse(token, "Bearer", userResponse, expirationMillis);
    }
}