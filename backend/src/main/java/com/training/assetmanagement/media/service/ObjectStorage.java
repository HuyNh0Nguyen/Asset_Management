package com.training.assetmanagement.media.service;

import java.io.InputStream;
import java.time.Duration;

public interface ObjectStorage {

    void put(String objectKey, InputStream content, long size, String contentType);

    void remove(String objectKey);

    // Không ném lỗi - dọn đối tượng sau commit / sau khi ghi dòng thất bại.
    // Tệp mồ côi vô hại vì không còn dòng nào cấp URL cho nó
    void removeQuietly(String objectKey);

    // URL ký tạm thời - API không bao giờ trả objectKey
    String presignedGetUrl(String objectKey);

    Duration presignedTtl();
}
