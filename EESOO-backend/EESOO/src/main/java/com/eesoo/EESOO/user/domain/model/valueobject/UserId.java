package com.eesoo.EESOO.user.domain.model.valueobject;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Objects;
import java.util.UUID;

public final class UserId {

    private final UUID value;


    private UserId(UUID value){
        this.value = value;
    }

    public static UserId create(){
        return new UserId(UuidCreator.getTimeOrderedEpoch());
    }


    public static UserId fromString(String id){
        return new UserId(UUID.fromString(id));
    }

    
    public UUID getValue(){
        return value;
    }


    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof UserId)) return false;
        UserId userId = (UserId) o ;
        return Objects.equals(value , userId.value);
    }

    @Override
    public int hashCode(){
        return Objects.hash(value);
    }

     @Override
    public String toString(){
        return value.toString();
    }
    

    
}
