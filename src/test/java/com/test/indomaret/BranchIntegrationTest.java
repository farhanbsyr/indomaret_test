package com.test.indomaret;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.indomaret.dto.request.BranchCreateRequest;
import com.test.indomaret.dto.request.BranchUpdateRequest;
import com.test.indomaret.entity.Branch;
import com.test.indomaret.entity.Province;
import com.test.indomaret.entity.Store;
import com.test.indomaret.repository.BranchRepository;
import com.test.indomaret.repository.ProvinceRepository;
import com.test.indomaret.repository.StoreRepository;
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
public class BranchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestAuthHelper authHelper;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private StoreRepository storeRepository;

    private String token;

    @BeforeEach
    public void setup() {
        token = authHelper.getAdminBearerToken();
    }

    @Test
    public void testCreateBranch() throws Exception {
        Province province = provinceRepository.findAll().get(0);

        BranchCreateRequest request = BranchCreateRequest.builder()
                .name("Cabang Baru Tangerang")
                .code("BR-TNG-" + System.currentTimeMillis())
                .address("Jl. Raya Serpong No. 88")
                .provinceId(province.getId())
                .build();

        mockMvc.perform(post("/api/v1/branches")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Cabang Baru Tangerang"))
                .andExpect(jsonPath("$.data.code").value(request.getCode()));
    }

    @Test
    public void testUpdateBranch() throws Exception {
        Province province = provinceRepository.findAll().get(0);
        Branch branch = Branch.builder()
                .name("Cabang Lama Sebelum Update")
                .code("BR-UPD-" + System.currentTimeMillis())
                .address("Jl. Lama No. 1")
                .province(province)
                .build();
        branch.setIsActive(true);
        branch.setIsDeleted(false);
        Branch saved = branchRepository.save(branch);

        BranchUpdateRequest updateRequest = BranchUpdateRequest.builder()
                .name("Cabang Baru Sukses Diupdate")
                .address("Jl. Baru No. 99")
                .build();

        mockMvc.perform(put("/api/v1/branches/" + saved.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Cabang Baru Sukses Diupdate"))
                .andExpect(jsonPath("$.data.address").value("Jl. Baru No. 99"));

        Branch updated = branchRepository.findById(saved.getId()).orElseThrow();
        assertTrue(updated.getName().equals("Cabang Baru Sukses Diupdate"));
    }

    @Test
    public void testDeleteBranchSoftDeleteAndCascade() throws Exception {
        Province province = provinceRepository.findAll().get(0);
        Branch branch = Branch.builder()
                .name("Cabang Akan Dihapus")
                .code("BR-DEL-" + System.currentTimeMillis())
                .address("Jl. Hapus No. 13")
                .province(province)
                .build();
        branch.setIsActive(true);
        branch.setIsDeleted(false);
        Branch savedBranch = branchRepository.save(branch);

        Store store = Store.builder()
                .name("Toko Pada Cabang Yang Akan Dihapus")
                .code("STR-CASC-" + System.currentTimeMillis())
                .address("Jl. Toko No. 1")
                .branch(savedBranch)
                .build();
        store.setIsActive(true);
        store.setIsDeleted(false);
        Store savedStore = storeRepository.save(store);

        // Delete branch
        mockMvc.perform(delete("/api/v1/branches/" + savedBranch.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify Branch soft delete in DB
        Branch deletedBranch = branchRepository.findById(savedBranch.getId()).orElseThrow();
        assertTrue(deletedBranch.getIsDeleted());
        assertFalse(deletedBranch.getIsActive());

        // Verify GET /branches/{id} returns 404 (non-deleted only)
        mockMvc.perform(get("/api/v1/branches/" + savedBranch.getId())
                        .header("Authorization", token))
                .andExpect(status().isNotFound());

        // Verify GET /branches does not contain deleted branch
        mockMvc.perform(get("/api/v1/branches")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].code", not(hasItem(savedBranch.getCode()))));

        // Verify associated store is also excluded
        mockMvc.perform(get("/api/v1/stores/" + savedStore.getId())
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }
}
