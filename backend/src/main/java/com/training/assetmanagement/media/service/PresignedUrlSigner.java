package com.training.assetmanagement.media.service;

// URL ký tạm thời - bucket private, API không bao giờ trả objectKey
public interface PresignedUrlSigner {

    String thumbnailUrl(String objectKey);

    String originalUrl(String objectKey);
}
