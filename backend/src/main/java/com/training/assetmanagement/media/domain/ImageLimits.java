package com.training.assetmanagement.media.domain;

public final class ImageLimits {

    // Khớp spring.servlet.multipart.max-file-size
    public static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
    // PNG 5 MB có thể giải mã thành 20 000 x 20 000 - chặn trước khi giải mã toàn bộ
    public static final long MAX_PIXELS = 40_000_000L;
    public static final float JPEG_QUALITY = 0.85f;

    private ImageLimits() {}
}
