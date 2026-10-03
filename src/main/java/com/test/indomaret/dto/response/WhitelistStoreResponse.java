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
public class WhitelistStoreResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private String storeCode;
    private String storeAddress;
    private Long branchId;
    private String branchName;
    private Long provinceId;
    private String provinceName;
    private String reason;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
