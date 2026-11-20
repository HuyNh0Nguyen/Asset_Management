package com.training.assetmanagement.media.service;

// URL ký tạm thời (app.storage.presigned-ttl) - đường đọc ảnh duy nhất, bucket private
public interface PresignedUrlSigner {

    String thumbnailUrl(String objectKey);

    String originalUrl(String objectKey);
}
