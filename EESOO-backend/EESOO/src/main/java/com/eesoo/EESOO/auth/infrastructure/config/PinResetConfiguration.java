package com.eesoo.EESOO.auth.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eesoo.EESOO.auth.domain.model.valueobject.PinResetAttemptLifetime;

@Configuration
@EnableConfigurationProperties(
        PinResetProperties.class
)
public class PinResetConfiguration {

    @Bean
    public PinResetAttemptLifetime pinResetAttemptLifetime(
            PinResetProperties properties
    ) {
        return PinResetAttemptLifetime.of(
                properties.getFirstFailureDuration(),
                properties.getResetDuration()
        );
    }
}
