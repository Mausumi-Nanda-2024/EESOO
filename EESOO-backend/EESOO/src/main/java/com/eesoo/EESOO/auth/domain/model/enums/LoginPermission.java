package com.eesoo.EESOO.auth.domain.model.enums;

import org.springframework.modulith.NamedInterface;

@NamedInterface("user-spi")
public enum LoginPermission {

    ELIGIBLE("User can login"),
    PENDING_VERIFICATION("User can login but verification is pending"),
    ACCOUNT_DELETED("User account has been deleted"),
    ACCOUNT_LOCKED("User account is locked"),
    ACCOUNT_SUSPENDED("User account is suspended");

    private final String reason;

    LoginPermission(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public boolean allowsLogin() {
        return this == ELIGIBLE
                || this == PENDING_VERIFICATION;
    }


    
}
