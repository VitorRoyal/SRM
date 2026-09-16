package com.srm.creditengine.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    public static final Instant FIXED_NOW = Instant.parse("2026-09-14T15:00:00Z");
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
    }

    @Bean
    @Primary
    Clock fixedClock() {
        return Clock.fixed(FIXED_NOW, BUSINESS_ZONE);
    }
}
