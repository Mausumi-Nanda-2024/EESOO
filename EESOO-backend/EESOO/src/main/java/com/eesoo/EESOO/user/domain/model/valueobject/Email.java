package com.eesoo.EESOO.user.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final String value;

    private Email(String value){
        if(value != null && !value.isBlank()) {
            if(!EMAIL_PATTERN.matcher(value).matches()){
                throw new IllegalArgumentException("Invalid email format:" + value);
            }
            this.value = value.toLowerCase();
        } else {
            this.value = null; 
        }
    }

    public static Email of(String value){
        return new Email(value);
    }

    public boolean isPresent(){
        return value != null;
    }

    public String getValue(){
        return value;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true ;
        if(!(o instanceof Email)) return false ;
        Email email = (Email) o;
        return Objects.equals(value , email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString(){
        return value == null ? "(no email)" : value;
    }

    
}
