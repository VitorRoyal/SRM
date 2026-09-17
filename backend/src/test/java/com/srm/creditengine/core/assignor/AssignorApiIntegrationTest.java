package com.srm.creditengine.core.assignor;

import com.srm.creditengine.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssignorApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldListSeededAssignorsOrderedByName() throws Exception {
        mockMvc.perform(get("/assignors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains(
                        "Alfa Comercio de Tecidos Ltda",
                        "Beta Distribuidora de Alimentos S.A.",
                        "Gama Industria Metalurgica Ltda"
                )))
                .andExpect(jsonPath("$[0].taxId").value("11222333000181"));
    }
}
