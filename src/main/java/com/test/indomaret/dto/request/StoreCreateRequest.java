package com.test.indomaret.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class StoreCreateRequest {

    @NotBlank(message = "Store name is required")
    @Size(max = 150, message = "Store name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Store code is required")
    @Size(max = 50, message = "Store code must not exceed 50 characters")
    private String code;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotNull(message = "Branch ID is required")
    private Long branchId;
}
