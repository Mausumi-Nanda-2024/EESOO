package com.eesoo.EESOO.user.infrastructure.persistence.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.user.domain.model.entity.User;
import com.eesoo.EESOO.user.domain.model.enums.UserStatus;
import com.eesoo.EESOO.user.domain.model.valueobject.Email;
import com.eesoo.EESOO.user.domain.model.valueobject.FirstName;
import com.eesoo.EESOO.user.domain.model.valueobject.LastName;
import com.eesoo.EESOO.user.domain.model.valueobject.PhoneNumber;
import com.eesoo.EESOO.user.domain.model.valueobject.Pin;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;
import com.eesoo.EESOO.user.domain.model.valueobject.Username;
import com.eesoo.EESOO.user.infrastructure.persistence.entity.UserJpaEntity;

@Component
public class UserMapper {

    // Convert domain model -> JPA entity

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId().getValue());
        entity.setFirstName(user.getFirstName().getValue());
        entity.setLastName(user.getLastName().getValue());
        entity.setPhoneNumber(user.getPhoneNumber().getValue());
        entity.setUsername(user.getUsername().getValue());
        entity.setPin(user.getPin().getValue());

        if (user.getEmail() != null) {
            entity.setEmail(user.getEmail().getValue());
        }

        entity.setStatus(user.getStatus());
        entity.setUserRegisteredAt(user.getUserRegisteredAt());

        

        return entity;
    }

    // Convert JPA entity -> domain model
    public User toDomain(UserJpaEntity entity) {
        
        UserId userId = UserId.fromString(entity.getId().toString());
        Username username = Username.of(entity.getUsername());
        FirstName firstName = FirstName.of(entity.getFirstName());
        LastName lastName = LastName.of(entity.getLastName());
        Pin password = Pin.fromHashed(entity.getPin());
        Email email = entity.getEmail() != null ? Email.of(entity.getEmail()) : null;
        PhoneNumber phoneNumber = PhoneNumber.of(entity.getPhoneNumber());
        LocalDateTime registeredAt = entity.getUserRegisteredAt();
        UserStatus status = (entity.getStatus());

        return User.rehydrate(userId, username, firstName, lastName, password, email, phoneNumber, registeredAt, status);
    }
}