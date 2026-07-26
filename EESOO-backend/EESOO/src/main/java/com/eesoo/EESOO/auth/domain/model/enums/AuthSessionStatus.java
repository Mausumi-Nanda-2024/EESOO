package com.eesoo.EESOO.auth.domain.model.enums;

public enum AuthSessionStatus {

    ACTIVE,
    REVOKED,
    EXPIRED;

    public boolean isActive(){
        return this == ACTIVE;
    }
    
}
