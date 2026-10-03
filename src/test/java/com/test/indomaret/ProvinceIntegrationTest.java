package com.test.indomaret;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProvinceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestAuthHelper authHelper;

    private String token;

    @BeforeEach
    public void setup() {
        token = authHelper.getUserBearerToken();
    }

    @Test
    public void testGetAllProvinces() throws Exception {
        mockMvc.perform(get("/api/v1/provinces")
                        .header("Authorization", token)
                        .param("size", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(38)))
                .andExpect(jsonPath("$.data.content[0].name").isNotEmpty());
    }
}
