package com.test.indomaret.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhitelistStoreCreateRequest {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
}
