package com.training.assetmanagement.user.service;

import com.training.assetmanagement.media.service.ImageFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

// Không có tầng phạm vi riêng: Ai thấy tên người dùng thì thấy ảnh
@Service
@RequiredArgsConstructor
public class AvatarUrlResolverImpl implements AvatarUrlResolver {

    private final ImageFileService imageFileService;

    @Override
    public Map<UUID, String> urlsFor(Collection<UUID> avatarImageIds) {
        return avatarImageIds.isEmpty() ? Map.of() : imageFileService.thumbnailUrls(avatarImageIds);
    }

    @Override
    public Optional<String> urlFor(UUID avatarImageId) {
        if (avatarImageId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(urlsFor(List.of(avatarImageId)).get(avatarImageId));
    }
}
