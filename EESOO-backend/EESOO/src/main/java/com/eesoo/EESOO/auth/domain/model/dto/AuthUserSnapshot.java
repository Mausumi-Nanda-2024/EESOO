package com.eesoo.EESOO.auth.domain.model.dto;

import java.util.Objects;
import java.util.UUID;

import org.springframework.modulith.NamedInterface;

import com.eesoo.EESOO.auth.domain.model.enums.LoginPermission;

@NamedInterface("user-spi")
public final class AuthUserSnapshot {

    private final UUID userId;
    private final String username;
    private final LoginPermission loginPermission;

    public AuthUserSnapshot(UUID userId, String username, LoginPermission loginPermission) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.username = Objects.requireNonNull(username, "username cannot be null");
        this.loginPermission = Objects.requireNonNull(loginPermission, "loginPermission cannot be null");
    }

    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public LoginPermission getLoginPermission() { return loginPermission; }
}
    

