package com.srm.creditengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${credit-engine.business-zone}") String businessZone) {
        return Clock.system(ZoneId.of(businessZone));
    }
}
