package com.eesoo.EESOO.user.application.register.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.eesoo.EESOO.user.application.exception.EmailAlreadyExistsException;
import com.eesoo.EESOO.user.application.exception.PhoneNumberAlreadyExistsException;
import com.eesoo.EESOO.user.application.exception.UsernameGenerationFailedException;
import com.eesoo.EESOO.user.application.register.command.RegisterUserCommand;
import com.eesoo.EESOO.user.domain.model.entity.User;
import com.eesoo.EESOO.user.domain.model.valueobject.Email;
import com.eesoo.EESOO.user.domain.model.valueobject.FirstName;
import com.eesoo.EESOO.user.domain.model.valueobject.LastName;
import com.eesoo.EESOO.user.domain.model.valueobject.PhoneNumber;
import com.eesoo.EESOO.user.domain.model.valueobject.Pin;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;
import com.eesoo.EESOO.user.domain.model.valueobject.Username;
import com.eesoo.EESOO.user.domain.repository.UserRepository;
import com.eesoo.EESOO.user.domain.service.PinEncoder;
import com.eesoo.EESOO.shared.domain.time.TimeProvider;
import com.eesoo.EESOO.user.domain.service.UsernameGenerator;

@Service
public class UserCreationService {

    private static final int MAX_USERNAME_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final TimeProvider timeProvider;
    private final PinEncoder pinEncoder;
    private final TransactionTemplate transactionTemplate;

    public UserCreationService(
            UserRepository userRepository,
            UsernameGenerator usernameGenerator,
            TimeProvider timeProvider,
            PinEncoder pinEncoder,
            PlatformTransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.usernameGenerator = usernameGenerator;
        this.timeProvider = timeProvider;
        this.pinEncoder = pinEncoder;

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate = template;
    }

    public User register(RegisterUserCommand command) {
        UserId userId = UserId.create();

        FirstName firstName = FirstName.of(command.getFirstName());
        LastName lastName = LastName.of(command.getLastName());
        PhoneNumber phoneNumber = PhoneNumber.of(command.getPhoneNumber());

        if (userRepository.phoneNumberExists(phoneNumber.getValue())) {
             throw new PhoneNumberAlreadyExistsException(phoneNumber.getValue());
        }

        Email email = (command.getEmail() != null && !command.getEmail().isBlank())
                ? Email.of(command.getEmail())
                : null;

        if (email != null && userRepository.emailExists(email.getValue())) {
            throw new EmailAlreadyExistsException(email.getValue());
        }

        Pin pin = Pin.fromRaw(command.getPin(), pinEncoder);

        for (int attempt = 0; attempt < MAX_USERNAME_ATTEMPTS; attempt++) {
            Username username = usernameGenerator.generate(firstName.getValue());

            if (userRepository.usernameExists(username.getValue())) {
                continue;
            }

            User user = User.register(
                    userId,
                    username,
                    firstName,
                    lastName,
                    phoneNumber,
                    pin,
                    email,
                    timeProvider);

            Boolean saved = transactionTemplate.execute(status -> userRepository.saveIfUsernameAvailable(user));
            if (Boolean.TRUE.equals(saved)) {
                return user;
            }
        }

         throw new UsernameGenerationFailedException();
    }
}
