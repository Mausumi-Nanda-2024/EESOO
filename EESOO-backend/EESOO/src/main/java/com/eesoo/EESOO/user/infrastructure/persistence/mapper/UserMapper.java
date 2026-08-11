package com.eesoo.EESOO.user.infrastructure.persistence.mapper;

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

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId().getValue(),
                user.getUsername().getValue(),
                user.getFirstName().getValue(),
                user.getLastName().getValue(),
                user.getPin().getValue(),
                user.getEmail() != null ? user.getEmail().getValue() : null,
                user.getPhoneNumber().getValue(),
                user.getStatus().name(),
                user.getUserRegisteredAt()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        return User.rehydrate(
                UserId.fromString(entity.getId().toString()),
                Username.of(entity.getUsername()),
                FirstName.of(entity.getFirstName()),
                LastName.of(entity.getLastName()),
                Pin.fromHashed(entity.getPin()),
                entity.getEmail() != null ? Email.of(entity.getEmail()) : null,
                PhoneNumber.of(entity.getPhoneNumber()),
                entity.getUserRegisteredAt(),
                UserStatus.valueOf(entity.getStatus())
        );
    }
}
