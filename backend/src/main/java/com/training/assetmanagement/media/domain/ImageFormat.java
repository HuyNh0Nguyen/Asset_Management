package com.training.assetmanagement.media.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

// Kiểu ảnh nhận - xác định bằng chữ ký đầu tệp, bỏ qua tên tệp và Content-Type. Không nhận SVG
@Getter
@RequiredArgsConstructor
public enum ImageFormat {

    JPEG("jpeg"),
    PNG("png"),
    WEBP("webp");       // Giải mã qua TwelveMonkeys

    // Tên format của ImageIO
    private final String readerName;

    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_MAGIC = {0x52, 0x49, 0x46, 0x46};      // "RIFF"
    private static final byte[] WEBP_MAGIC = {0x57, 0x45, 0x42, 0x50};      // "WEBP" ở byte thứ 8
    private static final int WEBP_MAGIC_OFFSET = 8;

    public static Optional<ImageFormat> detect(byte[] content) {
        if (startsWith(content, JPEG_MAGIC, 0)) {
            return Optional.of(JPEG);
        }
        if (startsWith(content, PNG_MAGIC, 0)) {
            return Optional.of(PNG);
        }
        if (startsWith(content, RIFF_MAGIC, 0) && startsWith(content, WEBP_MAGIC, WEBP_MAGIC_OFFSET)) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] content, byte[] magic, int offset) {
        return content.length >= offset + magic.length
            && Arrays.equals(content, offset, offset + magic.length, magic, 0, magic.length);
    }
}
