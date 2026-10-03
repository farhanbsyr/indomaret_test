package com.test.indomaret.controller;

import com.test.indomaret.dto.request.StoreCreateRequest;
import com.test.indomaret.dto.request.StoreUpdateRequest;
import com.test.indomaret.dto.response.ApiResponse;
import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.dto.response.StoreResponse;
import com.test.indomaret.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<StoreResponse>>> searchStoresByProvince(
            @RequestParam(required = false) String provinceName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PagedResponse<StoreResponse> response = storeService.searchStoresByProvince(provinceName, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<StoreResponse>>> getAllStores(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PagedResponse<StoreResponse> response = storeService.getAllStores(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long id) {
        StoreResponse response = storeService.getStoreById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody StoreCreateRequest request) {
        StoreResponse response = storeService.createStore(request);
        return new ResponseEntity<>(ApiResponse.success("Store created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody StoreUpdateRequest request) {
        StoreResponse response = storeService.updateStore(id, request);
        return ResponseEntity.ok(ApiResponse.success("Store updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.ok(ApiResponse.success("Store deleted successfully", null));
    }
}
