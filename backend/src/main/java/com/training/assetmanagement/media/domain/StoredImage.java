package com.training.assetmanagement.media.domain;

import io.swagger.v3.oas.annotations.media.Schema;

// Ảnh đã tải lên MinIO (bản chính + bản thu nhỏ), chưa có dòng image_files
@Schema(name = "StoredImage", description = "Stored image")
public record StoredImage(String objectKey) {
}
