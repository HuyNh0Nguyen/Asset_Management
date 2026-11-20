package com.training.assetmanagement.media.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.geom.AffineTransform;
import java.util.Arrays;

// Tám giá trị thẻ EXIF Orientation -> phép biến đổi đưa ảnh về đúng chiều hiển thị
@Getter
@RequiredArgsConstructor
public enum ExifOrientation {

    NORMAL(1),
    FLIP_HORIZONTAL(2),
    ROTATE_180(3),
    FLIP_VERTICAL(4),
    TRANSPOSE(5),
    ROTATE_90_CW(6),
    TRANSVERSE(7),
    ROTATE_270_CW(8);

    private final int tagValue;

    // Giá trị lạ / thiếu -> không xoay
    public static ExifOrientation fromTag(int tagValue) {
        return Arrays.stream(values()).filter(item -> item.tagValue == tagValue).findFirst().orElse(NORMAL);
    }

    // 5..8 đổi chỗ chiều rộng / cao
    public boolean swapsDimensions() {
        return tagValue >= TRANSPOSE.tagValue;
    }

    // AffineTransform(m00, m10, m01, m11, m02, m12): x' = m00·x + m01·y + m02, y' = m10·x + m11·y + m12
    public AffineTransform transform(int width, int height) {
        return switch (this) {
            case NORMAL -> new AffineTransform();
            case FLIP_HORIZONTAL -> new AffineTransform(-1, 0, 0, 1, width, 0);
            case ROTATE_180 -> new AffineTransform(-1, 0, 0, -1, width, height);
            case FLIP_VERTICAL -> new AffineTransform(1, 0, 0, -1, 0, height);
            case TRANSPOSE -> new AffineTransform(0, 1, 1, 0, 0, 0);
            case ROTATE_90_CW -> new AffineTransform(0, 1, -1, 0, height, 0);
            case TRANSVERSE -> new AffineTransform(0, -1, -1, 0, height, width);
            case ROTATE_270_CW -> new AffineTransform(0, -1, 1, 0, 0, width);
        };
    }
}
