package com.eesoo.EESOO.user.domain.model.entity;

import java.util.Objects;

import com.eesoo.EESOO.user.domain.model.enums.UserStatus;
import com.eesoo.EESOO.user.domain.model.valueobject.Email;
import com.eesoo.EESOO.user.domain.model.valueobject.FirstName;
import com.eesoo.EESOO.user.domain.model.valueobject.LastName;
import com.eesoo.EESOO.user.domain.model.valueobject.PhoneNumber;
import com.eesoo.EESOO.user.domain.model.valueobject.Pin;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;
import com.eesoo.EESOO.user.domain.model.valueobject.Username;
import com.eesoo.EESOO.shared.domain.time.TimeProvider;

import java.time.Instant;


public class User {

   private final UserId id;
   private final Username username;
   private final FirstName firstName;
   private final LastName lastName;
   private final Email email;
   private final Pin pin;
   private final PhoneNumber phoneNumber; 
   private final Instant userRegisteredAt;
   private UserStatus status;                                                

// Private constructor for registration
private User(
    UserId id,
    Username username,
    FirstName firstName,
    LastName lastName,
    Pin pin,
    Email email,
    PhoneNumber phoneNumber,
    Instant userRegisteredAt
) {
    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.pin = pin;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.userRegisteredAt = userRegisteredAt;
     this.status = UserStatus.PENDING_VERIFICATION;
}

// Constructor for rehydration
private User(
    UserId id,
    Username username,
    FirstName firstName,
    LastName lastName,
    Pin pin,
    Email email,
    PhoneNumber phoneNumber,
    Instant userRegisteredAt,
    UserStatus status
) {
    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.pin = pin;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.userRegisteredAt = userRegisteredAt;
    this.status = status;
}

public static User rehydrate(
    UserId id,
    Username username,
    FirstName firstName,
    LastName lastName,
    Pin pin,
    Email email,
    PhoneNumber phoneNumber,
    Instant registeredAt,
    UserStatus status
) {
    return new User(
        id, username, firstName, lastName,
        pin, email, phoneNumber,
        registeredAt, status
    );
}
   // Factory method for registration
   public static User register(
      UserId id , 
      Username username , 
      FirstName firstName , 
      LastName lastName , 
      PhoneNumber phoneNumber,
       Pin pin ,  
       Email email , 
       TimeProvider timeProvider
      )
   {
      return new User(
         id , username , firstName , lastName ,
          pin , email , phoneNumber , timeProvider.now());
   }

   public User replacePin(
      Pin newPin
   ) {
      if (newPin == null) {
         throw new IllegalArgumentException(
            "New PIN cannot be null"
         );
      }

      return new User(
         id,
         username,
         firstName,
         lastName,
         newPin,
         email,
         phoneNumber,
         userRegisteredAt,
         status
      );
   }

   public UserId getId(){
      return id;
   }

   public Username getUsername(){
      return username;

   }
   public FirstName getFirstName(){
      return firstName;
   }
   public LastName getLastName(){
      return lastName;
   }

   public Email getEmail(){
      return email;
   }

   public Pin getPin(){
      return pin;
   }

   public PhoneNumber getPhoneNumber(){
      return phoneNumber;
   }

   public UserStatus getStatus(){
      return status;
   }

   public Instant getUserRegisteredAt(){
      return userRegisteredAt;
   }

   // Identity check

   @Override
   public boolean equals(Object o){
      if(this == o) return true; 
      if(!(o instanceof User)) return false;
      User user = (User) o ; 
      return Objects.equals(id , user.id);
   }

  
   @Override
   public int hashCode(){
      return Objects.hash(id);
   }


}
   

