package com.eesoo.EESOO.auth.infrastructure.paseto.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eesoo.EESOO.auth.infrastructure.paseto.PasetoKeyProvider;

@Configuration
@EnableConfigurationProperties(
        PasetoProperties.class
)
public class PasetoConfiguration {

    @Bean
    public PasetoKeyProvider pasetoKeyProvider(
            PasetoProperties properties
    ) {
        return new PasetoKeyProvider(
                properties.getLocalKeyBase64()
        );
    }

}
