package com.example.demo.dto;

public class UserResponse {
    private String username;
    private boolean status;

    public UserResponse(String username, boolean status) {
        this.username = username;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public boolean isStatus() {
        return status;
    }
}
