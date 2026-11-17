package com.training.assetmanagement.organization.web.dto.request;

import com.training.assetmanagement.organization.domain.OrgCodeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

// Mã cùng định dạng với mã đơn vị
@Schema(name = "CreateRankRequest", description = "Request payload for creating a new rank")
public record CreateRankRequest(
    @NotBlank
    @Size(max = OrgCodeFormat.CODE_MAX_LENGTH)
    @Pattern(regexp = OrgCodeFormat.PATTERN)
    String code,

    @NotBlank
    @Size(max = OrgCodeFormat.NAME_MAX_LENGTH)
    String name,

    @NotNull
    @Positive
    Integer seniority
) {
}
