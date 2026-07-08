package com.eesoo.EESOO.user.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import com.eesoo.EESOO.user.domain.repository.UserRepository;
import com.eesoo.EESOO.user.infrastructure.persistence.repository.JpaUserRepository;
import com.eesoo.EESOO.user.infrastructure.pin.BycryptPinEncoder;
import com.eesoo.EESOO.user.infrastructure.time.TimeProviderImpl;
import com.eesoo.EESOO.user.domain.service.PinEncoder;
import com.eesoo.EESOO.user.domain.service.TimeProvider;
import com.eesoo.EESOO.user.domain.service.UsernameGenerator;

@Configuration
public class UserInfrastructureConfig {

    @Bean
    public UserRepository userRepository(JpaUserRepository impl) {
        return impl;
    }

    @Bean
    public TimeProvider timeProvider(TimeProviderImpl impl) {
        return impl;
    }
    
    @Bean
    public PinEncoder pinEncoder(BycryptPinEncoder impl) {
        return impl;
    }

    @Bean
    public UsernameGenerator usernameGenerator() {
        return new UsernameGenerator();
    }

}
