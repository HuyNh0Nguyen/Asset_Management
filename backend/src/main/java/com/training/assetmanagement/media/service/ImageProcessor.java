package com.training.assetmanagement.media.service;

import com.training.assetmanagement.media.domain.StoredImage;
import org.springframework.web.multipart.MultipartFile;

// Chạy NGOÀI giao dịch - xử lý ảnh và tải MinIO không giữ khóa CSDL nào
public interface ImageProcessor {

    // Chữ ký đầu tệp, ≤ 5 MB, ngưỡng điểm ảnh, xoay EXIF, tải hai bản ORIGINAL + THUMBNAIL
    StoredImage process(MultipartFile file);

    // Ghi dòng thất bại -> xóa hai đối tượng vừa tải
    void discard(StoredImage image);
}
