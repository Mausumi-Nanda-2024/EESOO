package com.eesoo.EESOO.shared.presentation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class web_config implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**") // apply CORS to all endpoints
                .allowedOrigins("*") // allow requests from this origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // allow these HTTP methods
                .allowedHeaders("*") // allow all headers
                .allowCredentials(false); // allow credentials (cookies, authorization headers, etc.)
                // .maxAge(3600); // set max age for preflight requests

    }
}
