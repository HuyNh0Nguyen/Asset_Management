package com.training.assetmanagement.catalog.web.dto.response;

import com.training.assetmanagement.catalog.domain.AttributeDataType;
import com.training.assetmanagement.catalog.domain.CatalogStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "AttributeDefinitionResponse", description = "Response for attribute definition operations")
public record AttributeDefinitionResponse(
    @Schema(requiredMode = REQUIRED) UUID id,           // FE cần: Trình sửa định nghĩa, sắp xếp phụ
    @Schema(requiredMode = REQUIRED) String code,       // Khóa trong attributes_json
    @Schema(requiredMode = REQUIRED) String label,
    @Schema(requiredMode = REQUIRED) AttributeDataType dataType,
    @Schema(requiredMode = REQUIRED) boolean required,
    String measureUnit,                                 // Chỉ có với NUMBER
    @Schema(requiredMode = REQUIRED) List<AttributeOptionResponse> options,
    @Schema(requiredMode = REQUIRED) boolean deadlineTracked,
    @Schema(requiredMode = REQUIRED) int displayOrder,
    @Schema(requiredMode = REQUIRED) CatalogStatus status
) {
}
