package com.eesoo.EESOO.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.eesoo.EESOO.user.domain.model.entity.User;


public interface UserRepository {

    User save(User user);
    boolean saveIfUsernameAvailable(User user);
    boolean usernameExists(String username);
    boolean phoneNumberExists(String phoneNumber);
    boolean emailExists(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findById(UUID userId);
    Optional<User> findForUpdateById(UUID userId);
    

}
