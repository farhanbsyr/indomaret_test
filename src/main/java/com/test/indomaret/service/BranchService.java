package com.test.indomaret.service;

import com.test.indomaret.dto.request.BranchCreateRequest;
import com.test.indomaret.dto.request.BranchUpdateRequest;
import com.test.indomaret.dto.response.BranchResponse;
import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.entity.Branch;
import com.test.indomaret.entity.Province;
import com.test.indomaret.entity.Store;
import com.test.indomaret.exception.BadRequestException;
import com.test.indomaret.exception.ResourceNotFoundException;
import com.test.indomaret.repository.BranchRepository;
import com.test.indomaret.repository.ProvinceRepository;
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
public class BranchService {

    private final BranchRepository branchRepository;
    private final ProvinceRepository provinceRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public BranchResponse createBranch(BranchCreateRequest request) {
        if (branchRepository.existsByCodeIgnoreCaseAndIsDeletedFalse(request.getCode())) {
            throw new BadRequestException("Branch with code '" + request.getCode() + "' already exists");
        }

        Province province = provinceRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(request.getProvinceId())
                .orElseThrow(() -> new ResourceNotFoundException("Province", "id", request.getProvinceId()));

        Branch branch = Branch.builder()
                .name(request.getName())
                .code(request.getCode())
                .address(request.getAddress())
                .province(province)
                .build();
        branch.setIsActive(true);
        branch.setIsDeleted(false);

        Branch savedBranch = branchRepository.save(branch);
        BranchResponse response = mapToResponse(savedBranch);

        auditLogService.recordChange("Branch", savedBranch.getId(), "CREATE", null, response);

        return response;
    }

    @Transactional
    public BranchResponse updateBranch(Long id, BranchUpdateRequest request) {
        Branch branch = branchRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        BranchResponse oldState = mapToResponse(branch);

        if (request.getCode() != null && !request.getCode().equalsIgnoreCase(branch.getCode())) {
            if (branchRepository.existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(request.getCode(), id)) {
                throw new BadRequestException("Branch with code '" + request.getCode() + "' already exists");
            }
            branch.setCode(request.getCode());
        }

        if (request.getName() != null) {
            branch.setName(request.getName());
        }

        if (request.getAddress() != null) {
            branch.setAddress(request.getAddress());
        }

        if (request.getProvinceId() != null && !request.getProvinceId().equals(branch.getProvince().getId())) {
            Province newProvince = provinceRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(request.getProvinceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Province", "id", request.getProvinceId()));
            branch.setProvince(newProvince);
        }

        if (request.getIsActive() != null) {
            branch.setIsActive(request.getIsActive());
        }

        Branch updatedBranch = branchRepository.save(branch);
        BranchResponse newState = mapToResponse(updatedBranch);

        auditLogService.recordChange("Branch", branch.getId(), "UPDATE", oldState, newState);

        return newState;
    }

    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        BranchResponse oldState = mapToResponse(branch);

        // Soft delete branch
        branch.setIsDeleted(true);
        branch.setIsActive(false);

        // Cascade soft delete to associated stores
        if (branch.getStores() != null) {
            for (Store store : branch.getStores()) {
                store.setIsDeleted(true);
                store.setIsActive(false);
            }
        }

        branchRepository.save(branch);

        auditLogService.recordChange("Branch", id, "DELETE", oldState, "SOFT_DELETED_WITH_STORES");
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findActiveByIdWithProvince(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        return mapToResponse(branch);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BranchResponse> getAllBranches(Pageable pageable) {
        Page<Branch> page = branchRepository.findAllActiveWithProvince(pageable);
        List<BranchResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        return PagedResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BranchResponse> getBranchesByProvince(Long provinceId, Pageable pageable) {
        Page<Branch> page = branchRepository.findAllByProvinceIdActive(provinceId, pageable);
        List<BranchResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        return PagedResponse.from(page, content);
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .code(branch.getCode())
                .address(branch.getAddress())
                .provinceId(branch.getProvince() != null ? branch.getProvince().getId() : null)
                .provinceName(branch.getProvince() != null ? branch.getProvince().getName() : null)
                .provinceCode(branch.getProvince() != null ? branch.getProvince().getCode() : null)
                .isActive(branch.getIsActive())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }
}
