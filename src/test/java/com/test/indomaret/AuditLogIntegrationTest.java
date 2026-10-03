package com.test.indomaret;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.indomaret.dto.request.BranchCreateRequest;
import com.test.indomaret.entity.Province;
import com.test.indomaret.repository.AuditLogRepository;
import com.test.indomaret.repository.ProvinceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuditLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestAuthHelper authHelper;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String token;

    @BeforeEach
    public void setup() {
        token = authHelper.getAdminBearerToken();
    }

    @Test
    public void testUserChangesAreLoggedForAudit() throws Exception {
        Province province = provinceRepository.findAll().get(0);

        BranchCreateRequest request = BranchCreateRequest.builder()
                .name("Cabang Audit Trail Test")
                .code("BR-AUDIT-" + System.currentTimeMillis())
                .address("Jl. Audit No. 123")
                .provinceId(province.getId())
                .build();

        // Perform branch creation
        mockMvc.perform(post("/api/v1/branches")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Verify audit log has recorded the action
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.data.content[*].action", hasItem("CREATE")))
                .andExpect(jsonPath("$.data.content[*].entityName", hasItem("Branch")))
                .andExpect(jsonPath("$.data.content[*].performedBy", hasItem("admin")));
    }
}
