package com.example.demo.dto;



public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UserInfo user
) {
    public record UserInfo(
        long userId,
        String username,
        String email,
        String role
    ) {}
}
