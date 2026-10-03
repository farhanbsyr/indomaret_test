package com.test.indomaret.controller;

import com.test.indomaret.dto.response.ApiResponse;
import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.dto.response.ProvinceResponse;
import com.test.indomaret.service.ProvinceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/provinces")
@RequiredArgsConstructor
public class ProvinceController {

    private final ProvinceService provinceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProvinceResponse>>> getAllProvinces(
            @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<ProvinceResponse> response = provinceService.getAllProvinces(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProvinceResponse>> getProvinceById(@PathVariable Long id) {
        ProvinceResponse response = provinceService.getProvinceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
