package com.eesoo.EESOO.user.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.eesoo.EESOO.user.domain.model.entity.User;
import com.eesoo.EESOO.user.domain.repository.UserRepository;
import com.eesoo.EESOO.user.infrastructure.persistence.mapper.UserMapper;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springRepo;
    private final UserMapper mapper;

    public JpaUserRepository(SpringDataUserRepository springRepo, UserMapper mapper) {
        this.springRepo = springRepo;
        this.mapper = mapper;
    }

    @Override
    public boolean saveIfUsernameAvailable(User user) {
        try {
            springRepo.saveAndFlush(mapper.toEntity(user));
            return true;
        } catch (DataIntegrityViolationException ex) {
            if (isUsernameDuplicate(ex)) {
                return false;
            }
            throw ex;
        }
    }

    @Override
    public boolean usernameExists(String username) {
        return springRepo.existsByUsername(username);
    }

    @Override
    public boolean phoneNumberExists(String phoneNumber) {
        return springRepo.existsByPhoneNumber(phoneNumber);
    }


    @Override
    public boolean emailExists(String email) {
        return springRepo.existsByEmail(email);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return springRepo.findByPhoneNumber(phoneNumber)
                .map(mapper::toDomain);
    }   

    @Override   
    public Optional<User> findById(UUID userId) {
    return springRepo.findById(userId)
            .map(mapper::toDomain);
}

    private boolean isUsernameDuplicate(DataIntegrityViolationException ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("username") && (lower.contains("unique") || lower.contains("duplicate"))) {
                    return true;
                }
                if (lower.contains("users_username_key")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

}
