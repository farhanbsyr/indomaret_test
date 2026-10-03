package com.test.indomaret;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.indomaret.dto.request.WhitelistStoreCreateRequest;
import com.test.indomaret.dto.request.WhitelistStoreUpdateRequest;
import com.test.indomaret.entity.Branch;
import com.test.indomaret.entity.Store;
import com.test.indomaret.repository.BranchRepository;
import com.test.indomaret.repository.StoreRepository;
import com.test.indomaret.repository.WhitelistStoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WhitelistStoreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestAuthHelper authHelper;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private WhitelistStoreRepository whitelistStoreRepository;

    private String token;

    @BeforeEach
    public void setup() {
        token = authHelper.getAdminBearerToken();
    }

    @Test
    public void testWhitelistCrudLifecycle() throws Exception {
        Branch branch = branchRepository.findAll().get(0);
        Store store = Store.builder()
                .name("Toko Calon Whitelist")
                .code("STR-WL-TEST-" + System.currentTimeMillis())
                .address("Jl. Whitelist No. 77")
                .branch(branch)
                .build();
        store.setIsActive(true);
        store.setIsDeleted(false);
        Store savedStore = storeRepository.save(store);

        // 1. Add to whitelist
        WhitelistStoreCreateRequest addRequest = WhitelistStoreCreateRequest.builder()
                .storeId(savedStore.getId())
                .reason("Important Strategic Location")
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/whitelist-stores")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.storeId").value(savedStore.getId()))
                .andExpect(jsonPath("$.data.reason").value("Important Strategic Location"))
                .andReturn().getResponse().getContentAsString();

        Long whitelistId = objectMapper.readTree(responseJson).get("data").get("id").asLong();

        // 2. List whitelist stores
        mockMvc.perform(get("/api/v1/whitelist-stores")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].storeCode", hasItem(savedStore.getCode())));

        // 3. Update whitelist store
        WhitelistStoreUpdateRequest updateRequest = WhitelistStoreUpdateRequest.builder()
                .reason("Updated Strategic Reason 2026")
                .build();

        mockMvc.perform(put("/api/v1/whitelist-stores/" + whitelistId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reason").value("Updated Strategic Reason 2026"));

        // 4. Remove from whitelist
        mockMvc.perform(delete("/api/v1/whitelist-stores/" + whitelistId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var wsInDb = whitelistStoreRepository.findById(whitelistId).orElseThrow();
        assertTrue(wsInDb.getIsDeleted());
        assertFalse(wsInDb.getIsActive());

        // 5. Verify removed store no longer appears in GET /api/v1/whitelist-stores
        mockMvc.perform(get("/api/v1/whitelist-stores")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].id", not(hasItem(whitelistId.intValue()))));
    }
}
