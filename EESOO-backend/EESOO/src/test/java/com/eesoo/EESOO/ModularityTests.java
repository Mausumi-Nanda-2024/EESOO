package com.eesoo.EESOO;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTests {

     ApplicationModules modules = ApplicationModules.of(EesooApplication.class);

    @Test
    void shouldFollowModuleBoundaries() {
        modules.verify();
    }
    
}
