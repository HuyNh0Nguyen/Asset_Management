package com.training.assetmanagement.media.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Hai đối tượng MinIO của một ảnh; khóa bản thu nhỏ suy ra bằng quy ước thumb/<khóa>
@Getter
@RequiredArgsConstructor
public enum ImageVariant {

    ORIGINAL("", 1600),
    THUMBNAIL("thumb/", 320);       // Ảnh đại diện luôn dùng bản này

    private final String keyPrefix;
    private final int maxEdge;

    public String keyOf(String objectKey) {
        return keyPrefix + objectKey;
    }
}
