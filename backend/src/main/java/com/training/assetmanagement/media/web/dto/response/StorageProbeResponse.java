package com.training.assetmanagement.media.web.dto.response;

import com.training.assetmanagement.media.service.StorageProbeResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "StorageProbeResponse", description = "Response for storage probe operations")
public record StorageProbeResponse(
    @Schema(requiredMode = REQUIRED) UUID probeId,          // FE cần để xin lại URL ký
    @Schema(requiredMode = REQUIRED) String url,
    @Schema(requiredMode = REQUIRED) OffsetDateTime expiresAt
) {

    public static StorageProbeResponse from(StorageProbeResult result) {
        return new StorageProbeResponse(result.probeId(), result.url(), result.expiresAt());
    }
}
