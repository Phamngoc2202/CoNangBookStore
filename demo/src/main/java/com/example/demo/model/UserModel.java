package com.example.demo.model;



public class UserModel{
    private String username;
    private String password;
    private boolean status;

    public UserModel(String username, String password, boolean status) {
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isStatus() {
        return status;
    }
}

