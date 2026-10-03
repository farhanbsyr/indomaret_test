package com.test.indomaret;

import com.test.indomaret.entity.Branch;
import com.test.indomaret.entity.Province;
import com.test.indomaret.entity.Store;
import com.test.indomaret.entity.WhitelistStore;
import com.test.indomaret.repository.BranchRepository;
import com.test.indomaret.repository.ProvinceRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StoreSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestAuthHelper authHelper;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private WhitelistStoreRepository whitelistStoreRepository;

    private String token;

    @BeforeEach
    public void setup() {
        token = authHelper.getAdminBearerToken();
    }

    @Test
    public void testSearchStoresByProvinceName() throws Exception {
        // Search stores for "DKI Jakarta"
        mockMvc.perform(get("/api/v1/stores/search")
                        .header("Authorization", token)
                        .param("provinceName", "DKI Jakarta")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", not(empty())));
    }

    @Test
    public void testWhitelistStoresAlwaysIncludedInSearchResult() throws Exception {
        // Create an explicit special store in Bali
        Province bali = provinceRepository.findByNameIgnoreCaseAndIsActiveTrueAndIsDeletedFalse("Bali")
                .orElseGet(() -> provinceRepository.save(Province.builder().name("Bali").code("BALI").build()));

        Branch baliBranch = branchRepository.findAllByProvinceIdActive(bali.getId(), null).getContent().stream()
                .findFirst()
                .orElseGet(() -> {
                    Branch b = Branch.builder().name("Cabang Bali Utama").code("BR-BALI-CUSTOM").province(bali).build();
                    b.setIsActive(true);
                    b.setIsDeleted(false);
                    return branchRepository.save(b);
                });

        Store baliStore = Store.builder()
                .name("Indomaret Kuta Flagship Whitelist")
                .code("STR-BALI-WL-" + System.currentTimeMillis())
                .address("Jl. Pantai Kuta")
                .branch(baliBranch)
                .build();
        baliStore.setIsActive(true);
        baliStore.setIsDeleted(false);
        Store savedBaliStore = storeRepository.save(baliStore);

        // Add Bali store to whitelist
        WhitelistStore ws = WhitelistStore.builder()
                .store(savedBaliStore)
                .reason("Global Famous Destination Store")
                .build();
        ws.setIsActive(true);
        ws.setIsDeleted(false);
        whitelistStoreRepository.save(ws);

        // Now search for "Aceh" province!
        // The Bali store MUST still appear in the results because it is whitelisted!
        mockMvc.perform(get("/api/v1/stores/search")
                        .header("Authorization", token)
                        .param("provinceName", "Aceh")
                        .param("size", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[*].code", hasItem(savedBaliStore.getCode())))
                .andExpect(jsonPath("$.data.content[?(@.code == '" + savedBaliStore.getCode() + "')].whitelisted", hasItem(true)));
    }

    @Test
    public void testInactiveAndDeletedStoresExcludedFromResponses() throws Exception {
        Province jabar = provinceRepository.findByNameIgnoreCaseAndIsActiveTrueAndIsDeletedFalse("Jawa Barat").orElseThrow();
        Branch jabarBranch = branchRepository.findAllByProvinceIdActive(jabar.getId(), null).getContent().get(0);

        // Create inactive store
        Store inactiveStore = Store.builder()
                .name("Inactive Store Bandung")
                .code("STR-INACT-" + System.currentTimeMillis())
                .branch(jabarBranch)
                .build();
        inactiveStore.setIsActive(false);
        inactiveStore.setIsDeleted(false);
        Store savedInactive = storeRepository.save(inactiveStore);

        // Create soft-deleted store
        Store deletedStore = Store.builder()
                .name("Deleted Store Bandung")
                .code("STR-DEL-" + System.currentTimeMillis())
                .branch(jabarBranch)
                .build();
        deletedStore.setIsActive(true);
        deletedStore.setIsDeleted(true);
        Store savedDeleted = storeRepository.save(deletedStore);

        // Search in Jawa Barat
        mockMvc.perform(get("/api/v1/stores/search")
                        .header("Authorization", token)
                        .param("provinceName", "Jawa Barat")
                        .param("size", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].code", not(hasItem(savedInactive.getCode()))))
                .andExpect(jsonPath("$.data.content[*].code", not(hasItem(savedDeleted.getCode()))));
    }
}
