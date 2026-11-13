package com.training.assetmanagement.media.service;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "StorageProbeResult", description = "Result of storage probe, including the probe ID, URL, and expiration time")
public record StorageProbeResult(UUID probeId, String url, OffsetDateTime expiresAt) {
}
