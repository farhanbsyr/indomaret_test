package com.test.indomaret.service;

import com.test.indomaret.dto.request.WhitelistStoreCreateRequest;
import com.test.indomaret.dto.request.WhitelistStoreUpdateRequest;
import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.dto.response.WhitelistStoreResponse;
import com.test.indomaret.entity.Branch;
import com.test.indomaret.entity.Province;
import com.test.indomaret.entity.Store;
import com.test.indomaret.entity.WhitelistStore;
import com.test.indomaret.exception.BadRequestException;
import com.test.indomaret.exception.ResourceNotFoundException;
import com.test.indomaret.repository.StoreRepository;
import com.test.indomaret.repository.WhitelistStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhitelistStoreService {

    private final WhitelistStoreRepository whitelistStoreRepository;
    private final StoreRepository storeRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public WhitelistStoreResponse addToWhitelist(WhitelistStoreCreateRequest request) {
        Store store = storeRepository.findActiveByIdWithHierarchy(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", request.getStoreId()));

        // Check if already in whitelist
        var existingOptional = whitelistStoreRepository.findByStoreId(request.getStoreId());
        WhitelistStore whitelistStore;

        if (existingOptional.isPresent()) {
            whitelistStore = existingOptional.get();
            if (!whitelistStore.getIsDeleted() && whitelistStore.getIsActive()) {
                throw new BadRequestException("Store with ID " + request.getStoreId() + " is already in the whitelist");
            }
            // Reactivate
            whitelistStore.setIsActive(true);
            whitelistStore.setIsDeleted(false);
            if (request.getReason() != null) {
                whitelistStore.setReason(request.getReason());
            }
        } else {
            whitelistStore = WhitelistStore.builder()
                    .store(store)
                    .reason(request.getReason())
                    .build();
            whitelistStore.setIsActive(true);
            whitelistStore.setIsDeleted(false);
        }

        WhitelistStore saved = whitelistStoreRepository.save(whitelistStore);
        WhitelistStoreResponse response = mapToResponse(saved);

        auditLogService.recordChange("WhitelistStore", saved.getId(), "ADD_WHITELIST", null, response);

        return response;
    }

    @Transactional
    public WhitelistStoreResponse updateWhitelist(Long id, WhitelistStoreUpdateRequest request) {
        WhitelistStore whitelistStore = whitelistStoreRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WhitelistStore", "id", id));

        WhitelistStoreResponse oldState = mapToResponse(whitelistStore);

        if (request.getReason() != null) {
            whitelistStore.setReason(request.getReason());
        }

        if (request.getIsActive() != null) {
            whitelistStore.setIsActive(request.getIsActive());
        }

        WhitelistStore updated = whitelistStoreRepository.save(whitelistStore);
        WhitelistStoreResponse newState = mapToResponse(updated);

        auditLogService.recordChange("WhitelistStore", id, "UPDATE_WHITELIST", oldState, newState);

        return newState;
    }

    @Transactional
    public void removeFromWhitelist(Long id) {
        WhitelistStore whitelistStore = whitelistStoreRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WhitelistStore", "id", id));

        WhitelistStoreResponse oldState = mapToResponse(whitelistStore);

        whitelistStore.setIsDeleted(true);
        whitelistStore.setIsActive(false);
        whitelistStoreRepository.save(whitelistStore);

        auditLogService.recordChange("WhitelistStore", id, "REMOVE_WHITELIST", oldState, "REMOVED");
    }

    @Transactional(readOnly = true)
    public WhitelistStoreResponse getWhitelistStoreById(Long id) {
        WhitelistStore ws = whitelistStoreRepository.findActiveByIdWithStore(id)
                .orElseThrow(() -> new ResourceNotFoundException("WhitelistStore", "id", id));
        return mapToResponse(ws);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WhitelistStoreResponse> getAllWhitelistStores(Pageable pageable) {
        Page<WhitelistStore> page = whitelistStoreRepository.findAllActiveWithStore(pageable);
        List<WhitelistStoreResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        return PagedResponse.from(page, content);
    }

    private WhitelistStoreResponse mapToResponse(WhitelistStore ws) {
        Store store = ws.getStore();
        Branch branch = store != null ? store.getBranch() : null;
        Province province = branch != null ? branch.getProvince() : null;

        return WhitelistStoreResponse.builder()
                .id(ws.getId())
                .storeId(store != null ? store.getId() : null)
                .storeName(store != null ? store.getName() : null)
                .storeCode(store != null ? store.getCode() : null)
                .storeAddress(store != null ? store.getAddress() : null)
                .branchId(branch != null ? branch.getId() : null)
                .branchName(branch != null ? branch.getName() : null)
                .provinceId(province != null ? province.getId() : null)
                .provinceName(province != null ? province.getName() : null)
                .reason(ws.getReason())
                .isActive(ws.getIsActive())
                .createdAt(ws.getCreatedAt())
                .updatedAt(ws.getUpdatedAt())
                .build();
    }
}
