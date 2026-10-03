package com.test.indomaret.controller;

import com.test.indomaret.dto.response.ApiResponse;
import com.test.indomaret.dto.response.AuditLogResponse;
import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogs(
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<AuditLogResponse> response = auditLogService.getAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
