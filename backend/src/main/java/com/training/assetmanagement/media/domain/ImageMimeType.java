package com.training.assetmanagement.media.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

// Kiểu ảnh hệ thống nhận; phase 2 ImageProcessor xác định bằng chữ ký đầu tệp, không tin Content-Type
@Getter
@RequiredArgsConstructor
public enum ImageMimeType {

    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp");

    private final String value;

    public static Optional<ImageMimeType> fromValue(String value) {
        return Arrays.stream(values()).filter(type -> type.value.equalsIgnoreCase(value)).findFirst();
    }
}
