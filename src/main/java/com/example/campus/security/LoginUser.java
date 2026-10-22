package com.example.campus.security;

import lombok.extern.java.Log;

public class LoginUser {
    private final Long userId;
    private final String username;

    public LoginUser(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
