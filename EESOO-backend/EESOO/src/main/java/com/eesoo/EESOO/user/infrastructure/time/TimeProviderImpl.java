package com.eesoo.EESOO.user.infrastructure.time;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.user.domain.service.TimeProvider;

@Component
public class TimeProviderImpl implements TimeProvider {

    @Override
    public LocalDateTime now(){
        return LocalDateTime.now();
    }

    
    
}
