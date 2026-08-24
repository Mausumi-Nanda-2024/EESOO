package com.eesoo.EESOO.shared.presentation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private int login;
    private int register;
    private int refresh;
    private int deviceStore;
    private int deviceCheck;
    private int pinResetConfirmMobile;
    private int pinResetIssue;

    

    
}
