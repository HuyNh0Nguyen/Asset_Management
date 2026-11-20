package com.training.assetmanagement.media.service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ImageFileService {

    // MANDATORY - ghi dòng image_files cho đối tượng đã tải; created_by = CurrentActor
    UUID register(String objectKey);

    // MANDATORY - xóa dòng ngay, đối tượng MinIO chỉ xóa SAU commit
    void delete(UUID imageFileId);

    // Một truy vấn cho cả lô: imageFileId -> URL ký bản thu nhỏ
    Map<UUID, String> thumbnailUrls(Collection<UUID> imageFileIds);
}
