package com.srm.creditengine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CreditEngineApplicationTests {

    @Test
    void contextLoads() {
    }
}
