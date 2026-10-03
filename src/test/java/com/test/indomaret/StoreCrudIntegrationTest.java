package com.test.indomaret;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.indomaret.dto.request.StoreCreateRequest;
import com.test.indomaret.dto.request.StoreUpdateRequest;
import com.test.indomaret.entity.Branch;
import com.test.indomaret.repository.BranchRepository;
import com.test.indomaret.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StoreCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestAuthHelper authHelper;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private StoreRepository storeRepository;

    private String token;

    @BeforeEach
    public void setup() {
        token = authHelper.getAdminBearerToken();
    }

    @Test
    public void testStoreCrudLifecycle() throws Exception {
        Branch branch = branchRepository.findAll().get(0);

        // 1. Create Store
        StoreCreateRequest createRequest = StoreCreateRequest.builder()
                .name("Indomaret CRUD Test")
                .code("STR-CRUD-" + System.currentTimeMillis())
                .address("Jl. Testing No. 42")
                .branchId(branch.getId())
                .build();

        String createJson = mockMvc.perform(post("/api/v1/stores")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Indomaret CRUD Test"))
                .andReturn().getResponse().getContentAsString();

        Long storeId = objectMapper.readTree(createJson).get("data").get("id").asLong();

        // 2. Read Store by ID
        mockMvc.perform(get("/api/v1/stores/" + storeId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(storeId));

        // 3. Update Store
        StoreUpdateRequest updateRequest = StoreUpdateRequest.builder()
                .name("Indomaret CRUD Test Updated")
                .address("Jl. Testing Updated No. 99")
                .build();

        mockMvc.perform(put("/api/v1/stores/" + storeId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Indomaret CRUD Test Updated"));

        // 4. Soft Delete Store
        mockMvc.perform(delete("/api/v1/stores/" + storeId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var storeInDb = storeRepository.findById(storeId).orElseThrow();
        assertTrue(storeInDb.getIsDeleted());
        assertFalse(storeInDb.getIsActive());

        // 5. Read deleted Store returns 404
        mockMvc.perform(get("/api/v1/stores/" + storeId)
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }
}
