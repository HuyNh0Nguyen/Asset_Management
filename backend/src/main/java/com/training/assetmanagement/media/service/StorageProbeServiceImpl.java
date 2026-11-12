package com.training.assetmanagement.media.service;

import com.training.assetmanagement.media.domain.ImageMimeType;
import com.training.assetmanagement.shared.config.AppProfile;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Profile({AppProfile.LOCAL, AppProfile.DEV})
@RequiredArgsConstructor
public class StorageProbeServiceImpl implements StorageProbeService {

    // Ảnh thử để riêng một tiền tố, không lẫn với ảnh nghiệp vụ
    private static final String PROBE_PREFIX = "probe/";

    private final ObjectStorage objectStorage;
    private final Clock clock;

    @Override
    public StorageProbeResult upload(MultipartFile file) {
        // Tạm tin Content-Type của trình duyệt - TODO (Phase 2) thay bằng ImageProcessor
        ImageMimeType mimeType = ImageMimeType.fromValue(file.getContentType())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE));
        UUID probeId = UUID.randomUUID();
        try (InputStream content = file.getInputStream()) {
            objectStorage.put(objectKey(probeId), content, file.getSize(), mimeType.getValue());
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.STORAGE_ERROR, ex.getMessage());
        }
        return sign(probeId);
    }

    @Override
    public StorageProbeResult refreshUrl(UUID probeId) {
        return sign(probeId);
    }

    private StorageProbeResult sign(UUID probeId) {
        String url = objectStorage.presignedGetUrl(objectKey(probeId));
        return new StorageProbeResult(probeId, url, OffsetDateTime.now(clock).plus(objectStorage.presignedTtl()));
    }

    private static String objectKey(UUID probeId) {
        return PROBE_PREFIX + probeId;
    }
}
