package com.training.assetmanagement.media.service;

import com.training.assetmanagement.media.domain.ImageVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresignedUrlSignerImpl implements PresignedUrlSigner {

    private final ObjectStorage objectStorage;

    // Khóa bản thu nhỏ theo quy ước thumb/<objectKey>
    @Override
    public String thumbnailUrl(String objectKey) {
        return objectStorage.presignedGetUrl(ImageVariant.THUMBNAIL.keyOf(objectKey));
    }

    @Override
    public String originalUrl(String objectKey) {
        return objectStorage.presignedGetUrl(ImageVariant.ORIGINAL.keyOf(objectKey));
    }
}
