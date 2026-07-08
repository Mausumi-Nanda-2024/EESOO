package com.eesoo.EESOO.user.application.exception;

public class UsernameGenerationFailedException extends RuntimeException {
    public UsernameGenerationFailedException() {
        super("Failed to generate a unique username after multiple attempts. Please try again.");
    }
    
}
