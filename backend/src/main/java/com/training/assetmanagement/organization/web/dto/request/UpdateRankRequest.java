package com.training.assetmanagement.organization.web.dto.request;

import com.training.assetmanagement.organization.domain.OrgCodeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Mã không có trong DTO -> gửi lên là 400 UNKNOWN_FIELD
public record UpdateRankRequest(
    @NotBlank
    @Size(max = OrgCodeFormat.NAME_MAX_LENGTH)
    String name,

    @NotNull
    @Positive
    Integer seniority
) {
}
