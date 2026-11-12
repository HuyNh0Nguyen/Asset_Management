package com.training.assetmanagement.media.service;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StorageProbeResult(UUID probeId, String url, OffsetDateTime expiresAt) {
}
