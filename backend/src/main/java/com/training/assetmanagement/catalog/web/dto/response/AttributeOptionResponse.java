package com.training.assetmanagement.catalog.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Một lựa chọn của thuộc tính SELECT: Mã cố định, nhãn sửa được
@Schema(name = "AttributeOptionResponse", description = "A selectable option for an attribute")
public record AttributeOptionResponse(
        @Schema(requiredMode = REQUIRED) String code,
        @Schema(requiredMode = REQUIRED) String label
) {
}
