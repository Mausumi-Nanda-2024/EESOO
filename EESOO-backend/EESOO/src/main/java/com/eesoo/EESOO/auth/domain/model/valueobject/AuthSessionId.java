package com.eesoo.EESOO.auth.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

public final class AuthSessionId {

    private final UUID value;

    private AuthSessionId(UUID value){
        this.value = value;
    }

    public static AuthSessionId create(){
        return new AuthSessionId(
            UuidCreator.getTimeOrderedEpoch()
        );
    }

    public static AuthSessionId fromString(String id){
        return new AuthSessionId(
            UUID.fromString(id)
        );
    }

    public UUID getValue(){
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AuthSessionId)) return false;

        AuthSessionId authSessionId = (AuthSessionId) object;
        return Objects.equals(value, authSessionId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
    
}
