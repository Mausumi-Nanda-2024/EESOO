package com.eesoo.EESOO.shared.presentation.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

     private final List<String> allowedOrigins;

    public CorsProperties(
            List<String> allowedOrigins
    ) {
        if (allowedOrigins == null
                || allowedOrigins.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one CORS origin must be configured"
            );
        }

        if (allowedOrigins.stream().anyMatch(
                origin -> origin == null
                        || origin.isBlank()
                       || origin.trim().equals("*")
        )) {
            throw new IllegalArgumentException(
                    "CORS origins cannot be blank or use wildcard *"
            );
        }

        this.allowedOrigins = allowedOrigins.stream()
                .map(String::trim)
                .distinct()
                .toList();
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
}
