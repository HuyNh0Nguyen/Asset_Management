package com.training.assetmanagement.organization.web.dto.request;

import com.training.assetmanagement.organization.domain.OrgCodeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Mã, loại, cha KHÔNG có trong DTO -> gửi lên là 400 UNKNOWN_FIELD; đổi cha đi đường /move
@Schema(name = "UpdateOrgUnitRequest", description = "Request payload for updating an organizational unit")
public record UpdateOrgUnitRequest(
    @NotBlank
    @Size(max = OrgCodeFormat.NAME_MAX_LENGTH)
    String name,

    @Schema(nullable = true, description = "null = leave empty")
    UUID headUserId
) {
}
