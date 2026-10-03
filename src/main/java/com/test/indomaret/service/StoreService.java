package com.test.indomaret.service;

import com.test.indomaret.config.AppProperties;
import com.test.indomaret.dto.request.StoreCreateRequest;
import com.test.indomaret.dto.request.StoreUpdateRequest;
import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.dto.response.StoreResponse;
import com.test.indomaret.entity.Branch;
import com.test.indomaret.entity.Store;
import com.test.indomaret.exception.BadRequestException;
import com.test.indomaret.exception.ResourceNotFoundException;
import com.test.indomaret.repository.BranchRepository;
import com.test.indomaret.repository.StoreRepository;
import com.test.indomaret.repository.WhitelistStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final BranchRepository branchRepository;
    private final WhitelistStoreRepository whitelistStoreRepository;
    private final AuditLogService auditLogService;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public PagedResponse<StoreResponse> searchStoresByProvince(
            String provinceName,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        boolean includeWhitelist = appProperties.getWhitelist().isAlwaysIncludeInSearch();

        Page<Store> storePage = storeRepository.searchStoresByProvince(
                provinceName != null ? provinceName.trim() : "",
                includeWhitelist,
                pageable
        );

        Set<Long> whitelistStoreIds = new HashSet<>(whitelistStoreRepository.findActiveWhitelistStoreIds());

        List<StoreResponse> content = storePage.getContent().stream()
                .map(store -> mapToResponse(store, whitelistStoreIds.contains(store.getId())))
                .toList();

        return PagedResponse.from(storePage, content);
    }

    @Transactional
    public StoreResponse createStore(StoreCreateRequest request) {
        if (storeRepository.existsByCodeIgnoreCaseAndIsDeletedFalse(request.getCode())) {
            throw new BadRequestException("Store with code '" + request.getCode() + "' already exists");
        }

        Branch branch = branchRepository.findActiveByIdWithProvince(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        Store store = Store.builder()
                .name(request.getName())
                .code(request.getCode())
                .address(request.getAddress())
                .branch(branch)
                .build();
        store.setIsActive(true);
        store.setIsDeleted(false);

        Store savedStore = storeRepository.save(store);
        StoreResponse response = mapToResponse(savedStore, false);

        auditLogService.recordChange("Store", savedStore.getId(), "CREATE", null, response);

        return response;
    }

    @Transactional
    public StoreResponse updateStore(Long id, StoreUpdateRequest request) {
        Store store = storeRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));

        StoreResponse oldState = mapToResponse(store, false);

        if (request.getCode() != null && !request.getCode().equalsIgnoreCase(store.getCode())) {
            if (storeRepository.existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(request.getCode(), id)) {
                throw new BadRequestException("Store with code '" + request.getCode() + "' already exists");
            }
            store.setCode(request.getCode());
        }

        if (request.getName() != null) {
            store.setName(request.getName());
        }

        if (request.getAddress() != null) {
            store.setAddress(request.getAddress());
        }

        if (request.getBranchId() != null && !request.getBranchId().equals(store.getBranch().getId())) {
            Branch newBranch = branchRepository.findActiveByIdWithProvince(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));
            store.setBranch(newBranch);
        }

        if (request.getIsActive() != null) {
            store.setIsActive(request.getIsActive());
        }

        Store updatedStore = storeRepository.save(store);
        boolean isWhitelisted = whitelistStoreRepository.existsByStoreIdAndIsDeletedFalse(updatedStore.getId());
        StoreResponse newState = mapToResponse(updatedStore, isWhitelisted);

        auditLogService.recordChange("Store", store.getId(), "UPDATE", oldState, newState);

        return newState;
    }

    @Transactional
    public void deleteStore(Long id) {
        Store store = storeRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));

        StoreResponse oldState = mapToResponse(store, false);

        store.setIsDeleted(true);
        store.setIsActive(false);

        // Also deactivate whitelist entry if any
        whitelistStoreRepository.findActiveByStoreId(id).ifPresent(ws -> {
            ws.setIsDeleted(true);
            ws.setIsActive(false);
            whitelistStoreRepository.save(ws);
        });

        storeRepository.save(store);

        auditLogService.recordChange("Store", id, "DELETE", oldState, "SOFT_DELETED");
    }

    @Transactional(readOnly = true)
    public StoreResponse getStoreById(Long id) {
        Store store = storeRepository.findActiveByIdWithHierarchy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
        boolean isWhitelisted = whitelistStoreRepository.existsByStoreIdAndIsDeletedFalse(id);
        return mapToResponse(store, isWhitelisted);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StoreResponse> getAllStores(Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Store> storePage = storeRepository.findAllActiveWithHierarchy(pageable);
        Set<Long> whitelistStoreIds = new HashSet<>(whitelistStoreRepository.findActiveWhitelistStoreIds());

        List<StoreResponse> content = storePage.getContent().stream()
                .map(store -> mapToResponse(store, whitelistStoreIds.contains(store.getId())))
                .toList();

        return PagedResponse.from(storePage, content);
    }

    private Pageable createPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : appProperties.getPagination().getDefaultPageSize();
        if (pageSize > appProperties.getPagination().getMaxPageSize()) {
            pageSize = appProperties.getPagination().getMaxPageSize();
        }

        String validSortBy = "name";
        if (sortBy != null) {
            String s = sortBy.trim().toLowerCase();
            if (s.equals("id") || s.equals("code") || s.equals("createdat")) {
                validSortBy = sortBy;
            }
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, validSortBy));
    }

    private StoreResponse mapToResponse(Store store, boolean isWhitelisted) {
        Branch branch = store.getBranch();
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .code(store.getCode())
                .address(store.getAddress())
                .branchId(branch != null ? branch.getId() : null)
                .branchName(branch != null ? branch.getName() : null)
                .branchCode(branch != null ? branch.getCode() : null)
                .provinceId(branch != null && branch.getProvince() != null ? branch.getProvince().getId() : null)
                .provinceName(branch != null && branch.getProvince() != null ? branch.getProvince().getName() : null)
                .provinceCode(branch != null && branch.getProvince() != null ? branch.getProvince().getCode() : null)
                .isWhitelisted(isWhitelisted)
                .isActive(store.getIsActive())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
