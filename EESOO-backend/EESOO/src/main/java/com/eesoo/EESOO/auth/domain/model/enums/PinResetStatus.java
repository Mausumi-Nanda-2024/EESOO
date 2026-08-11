package com.eesoo.EESOO.auth.domain.model.enums;

public enum PinResetStatus {

    FIRST_FAILURE,
    RESET_AVAILABLE,
    MOBILE_CONFIRMED,
    PIN_ISSUED;

    public boolean isFirstFailure() {
        return this == FIRST_FAILURE;
    }

    public boolean isResetAvailable() {
        return this == RESET_AVAILABLE;
    }

    public boolean isMobileConfirmed() {
        return this == MOBILE_CONFIRMED;
    }

    public boolean isPinIssued() {
        return this == PIN_ISSUED;
    }

    
}
