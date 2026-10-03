package com.test.indomaret.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchUpdateRequest {

    @Size(max = 150, message = "Branch name must not exceed 150 characters")
    private String name;

    @Size(max = 50, message = "Branch code must not exceed 50 characters")
    private String code;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private Long provinceId;

    private Boolean isActive;
}
