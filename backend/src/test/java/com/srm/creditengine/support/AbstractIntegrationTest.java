package com.srm.creditengine.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetReferenceRates() {
        jdbcTemplate.execute("TRUNCATE base_rate, exchange_rate RESTART IDENTITY CASCADE");
        jdbcTemplate.update(
                "INSERT INTO base_rate (monthly_rate, effective_at) VALUES (0.010000, '2020-01-01T00:00:00Z')"
        );
        jdbcTemplate.update(
                "INSERT INTO exchange_rate (currency, brl_per_unit, effective_at) VALUES ('USD', 5.43210000, '2020-01-01T00:00:00Z')"
        );
    }

    protected long countRows(String tableName) {
        Long rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        if (rowCount == null) {
            return 0;
        }
        return rowCount;
    }
}
