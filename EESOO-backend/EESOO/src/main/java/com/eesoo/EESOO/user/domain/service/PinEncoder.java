package com.eesoo.EESOO.user.domain.service;

public interface PinEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
    
}
