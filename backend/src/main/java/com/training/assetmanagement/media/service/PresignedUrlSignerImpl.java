package com.training.assetmanagement.media.service;

import com.training.assetmanagement.media.domain.ImageVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Chỉ biết quy ước khóa bản chính / thumb/<khóa>; việc ký do signingClient của StorageClientConfig
// (public-endpoint + khai vùng)
@Service
@RequiredArgsConstructor
public class PresignedUrlSignerImpl implements PresignedUrlSigner {

    private final ObjectStorage objectStorage;

    @Override
    public String thumbnailUrl(String objectKey) {
        return objectStorage.presignedGetUrl(ImageVariant.THUMBNAIL.keyOf(objectKey));
    }

    @Override
    public String originalUrl(String objectKey) {
        return objectStorage.presignedGetUrl(ImageVariant.ORIGINAL.keyOf(objectKey));
    }
}
