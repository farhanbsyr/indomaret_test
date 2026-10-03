package com.test.indomaret.controller;

import com.test.indomaret.dto.request.BranchCreateRequest;
import com.test.indomaret.dto.request.BranchUpdateRequest;
import com.test.indomaret.dto.response.ApiResponse;
import com.test.indomaret.dto.response.BranchResponse;
import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BranchResponse>>> getAllBranches(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<BranchResponse> response = branchService.getAllBranches(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Long id) {
        BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-province/{provinceId}")
    public ResponseEntity<ApiResponse<PagedResponse<BranchResponse>>> getBranchesByProvince(
            @PathVariable Long provinceId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<BranchResponse> response = branchService.getBranchesByProvince(provinceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody BranchCreateRequest request) {
        BranchResponse response = branchService.createBranch(request);
        return new ResponseEntity<>(ApiResponse.success("Branch created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchUpdateRequest request) {
        BranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully", null));
    }
}
