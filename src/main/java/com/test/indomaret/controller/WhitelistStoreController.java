package com.test.indomaret.controller;

import com.test.indomaret.dto.request.WhitelistStoreCreateRequest;
import com.test.indomaret.dto.request.WhitelistStoreUpdateRequest;
import com.test.indomaret.dto.response.ApiResponse;
import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.dto.response.WhitelistStoreResponse;
import com.test.indomaret.service.WhitelistStoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/whitelist-stores")
@RequiredArgsConstructor
public class WhitelistStoreController {

    private final WhitelistStoreService whitelistStoreService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<WhitelistStoreResponse>>> getAllWhitelistStores(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<WhitelistStoreResponse> response = whitelistStoreService.getAllWhitelistStores(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WhitelistStoreResponse>> getWhitelistStoreById(@PathVariable Long id) {
        WhitelistStoreResponse response = whitelistStoreService.getWhitelistStoreById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WhitelistStoreResponse>> addToWhitelist(
            @Valid @RequestBody WhitelistStoreCreateRequest request) {
        WhitelistStoreResponse response = whitelistStoreService.addToWhitelist(request);
        return new ResponseEntity<>(ApiResponse.success("Store added to whitelist successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WhitelistStoreResponse>> updateWhitelist(
            @PathVariable Long id,
            @Valid @RequestBody WhitelistStoreUpdateRequest request) {
        WhitelistStoreResponse response = whitelistStoreService.updateWhitelist(id, request);
        return ResponseEntity.ok(ApiResponse.success("Whitelist store updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFromWhitelist(@PathVariable Long id) {
        whitelistStoreService.removeFromWhitelist(id);
        return ResponseEntity.ok(ApiResponse.success("Store removed from whitelist successfully", null));
    }
}
