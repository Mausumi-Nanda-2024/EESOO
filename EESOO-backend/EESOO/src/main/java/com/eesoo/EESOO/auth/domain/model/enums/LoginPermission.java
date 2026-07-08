package com.eesoo.EESOO.auth.domain.model.enums;

public enum LoginPermission {

    ELIGIBLE("User can Login"),
    PENDING_VERIFICATION("User account is pending verification"),
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

    public boolean isEligible() {
        return this == ELIGIBLE;
    }


    
}
