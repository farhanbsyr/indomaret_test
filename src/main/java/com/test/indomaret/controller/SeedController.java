package com.test.indomaret.controller;

import com.test.indomaret.dto.response.ApiResponse;
import com.test.indomaret.service.DataSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seed")
@RequiredArgsConstructor
public class SeedController {

    private final DataSeederService dataSeederService;

    @PostMapping("/bulk-stores")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedBulkStores(
            @RequestParam(defaultValue = "20000") int count) {
        long startTime = System.currentTimeMillis();
        int totalStores = dataSeederService.seedBulkStores(count);
        long elapsedMs = System.currentTimeMillis() - startTime;

        Map<String, Object> result = new HashMap<>();
        result.put("totalStores", totalStores);
        result.put("targetCount", count);
        result.put("elapsedMs", elapsedMs);
        result.put("message", "Successfully seeded stores up to " + totalStores + " in " + elapsedMs + " ms");

        return ResponseEntity.ok(ApiResponse.success("Bulk seed completed", result));
    }
}
