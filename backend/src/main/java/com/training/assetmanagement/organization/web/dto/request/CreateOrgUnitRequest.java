package com.training.assetmanagement.organization.web.dto.request;

import com.training.assetmanagement.organization.domain.OrgUnitType;
import com.training.assetmanagement.shared.web.CodeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Không có trưởng: Đơn vị vừa tạo chưa có ai - gán qua Sửa
@Schema(name = "CreateOrgUnitRequest", description = "Request payload for creating a new organizational unit")
public record CreateOrgUnitRequest(
    @NotBlank
    @Size(max = CodeFormat.CODE_MAX_LENGTH)
    @Pattern(regexp = CodeFormat.PATTERN)
    String code,

    @NotBlank
    @Size(max = CodeFormat.NAME_MAX_LENGTH)
    String name,

    @NotNull
    OrgUnitType unitType,

    @NotNull
    UUID parentId
) {
}
