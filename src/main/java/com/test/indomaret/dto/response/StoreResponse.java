package com.test.indomaret.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private Long id;
    private String name;
    private String code;
    private String address;
    private Long branchId;
    private String branchName;
    private String branchCode;
    private Long provinceId;
    private String provinceName;
    private String provinceCode;
    private boolean isWhitelisted;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
