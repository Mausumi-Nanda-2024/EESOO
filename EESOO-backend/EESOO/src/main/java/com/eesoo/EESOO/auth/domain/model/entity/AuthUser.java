package com.eesoo.EESOO.auth.domain.model.entity;

import java.util.Objects;
import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.enums.LoginPermission;

public class AuthUser {

    private final UUID userId;
    private final String username;
    private final LoginPermission loginPermission;

    private AuthUser(UUID userId, String username, LoginPermission loginPermission) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.username = Objects.requireNonNull(username, "username cannot be null");
        this.loginPermission = Objects.requireNonNull(loginPermission, "loginPermission cannot be null");
    }

    public static AuthUser of(UUID userId, String username,LoginPermission loginPermission) {
        return new AuthUser(userId, username, loginPermission);
    }

    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean canLogin() { return loginPermission.isEligible();}
}