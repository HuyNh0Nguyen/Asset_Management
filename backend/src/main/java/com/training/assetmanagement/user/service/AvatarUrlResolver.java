package com.training.assetmanagement.user.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// avatarImageId -> URL ký bản thu nhỏ. API không bao giờ trả objectKey
public interface AvatarUrlResolver {

    // Một truy vấn image_files cho cả lô
    Map<UUID, String> urlsFor(Collection<UUID> avatarImageIds);

    Optional<String> urlFor(UUID avatarImageId);
}
