package com.eesoo.EESOO.user.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class PhoneNumber {

    private static final int PHONE_LENGTH = 10;
    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern INDIAN_MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    private final String value;

    private PhoneNumber(String value){
        this.value = value;
    }

    public static PhoneNumber of(String rawNumber){
        if(rawNumber == null || rawNumber.isBlank()){
            throw new IllegalArgumentException("Phone number cannot be null or blank.");
        }

        String trimmed = rawNumber.trim();

        if(trimmed.contains(" ")){
            throw new IllegalArgumentException("Phone number must not contain spaces");
        }

        if(trimmed.length() != PHONE_LENGTH){
            throw new IllegalArgumentException("Phone number must be exactly " + PHONE_LENGTH + " digits");
        }

        if(!DIGITS_ONLY_PATTERN.matcher(trimmed).matches()){
            throw new IllegalArgumentException("Phone number must contain only numeric digits");
        }

        if(!INDIAN_MOBILE_PATTERN.matcher(trimmed).matches()){
            throw new IllegalArgumentException("Phone number must be a valid Indian mobile number starting with 6, 7, 8, or 9");
        }

        return new PhoneNumber(trimmed);
    }

    public String getValue(){
        return value;
    }

    @Override
    public boolean equals(Object o){
        if(this == o)return true;
        if(!(o instanceof PhoneNumber)) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode(){
        return Objects.hash(value);
    }

    @Override
    public String toString(){
        return value;
    }

}
