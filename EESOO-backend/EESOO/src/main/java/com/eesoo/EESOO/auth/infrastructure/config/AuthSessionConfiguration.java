package com.eesoo.EESOO.auth.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionLifetime;

@Configuration
@EnableConfigurationProperties(
        AuthSessionProperties.class
)
public class AuthSessionConfiguration {

    @Bean
    public AuthSessionLifetime authSessionLifetime(
            AuthSessionProperties properties
    ) {
        return AuthSessionLifetime.of(
                properties.getDuration()
        );
    }
}
