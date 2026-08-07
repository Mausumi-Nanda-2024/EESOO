package com.eesoo.EESOO.user.application.register.dto;

import java.time.Instant;

import com.eesoo.EESOO.user.domain.model.entity.User;

public class RegisterUserResultDTO {

    private final String userId;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String email;
    private final String status;
    private final Instant userRegisteredAt;
    

    //constructor
    private RegisterUserResultDTO(String userId, String username, String firstName, String lastName, String phoneNumber, String email, String status, Instant userRegisteredAt ){
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.status = status;
        this.userRegisteredAt = userRegisteredAt;
    }

    public static RegisterUserResultDTO from(User user){
        return new RegisterUserResultDTO(
            user.getId().getValue().toString(),
            user.getUsername().getValue(),
            user.getFirstName().getValue(),
            user.getLastName().getValue(),
            user.getPhoneNumber().getValue(),
            user.getEmail() != null ? user.getEmail().getValue() : null,
            user.getStatus().name(),
            user.getUserRegisteredAt()
        );
    }

   
     public String getUserId(){
        return userId;
     }

        public String getUsername() {
            return username;
        }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public Instant getUserRegisteredAt() {
        return userRegisteredAt;
    }

}
