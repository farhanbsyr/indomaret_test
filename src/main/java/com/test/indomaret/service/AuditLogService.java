package com.test.indomaret.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.test.indomaret.config.AppProperties;
import com.test.indomaret.dto.response.AuditLogResponse;
import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.entity.AuditLog;
import com.test.indomaret.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordChange(String entityName, Long entityId, String action, Object oldValue, Object newValue) {
        if (!appProperties.getAudit().isEnabled()) {
            return;
        }

        try {
            String performedBy = getCurrentUsername();
            String ipAddress = getCurrentIpAddress();

            String oldValJson = oldValue != null ? objectMapper.writeValueAsString(oldValue) : null;
            String newValJson = newValue != null ? objectMapper.writeValueAsString(newValue) : null;

            AuditLog auditLog = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .performedBy(performedBy)
                    .oldValue(oldValJson)
                    .newValue(newValJson)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit logged: action={}, entity={}, id={}, performedBy={}", action, entityName, entityId, performedBy);
        } catch (Exception ex) {
            log.error("Failed to write audit log: {}", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogs(Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        List<AuditLogResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        return PagedResponse.from(page, content);
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .performedBy(log.getPerformedBy())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .timestamp(log.getTimestamp())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private String getCurrentIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
