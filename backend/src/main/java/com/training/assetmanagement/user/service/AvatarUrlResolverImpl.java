package com.training.assetmanagement.user.service;

import com.training.assetmanagement.media.service.ImageFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvatarUrlResolverImpl implements AvatarUrlResolver {

    private final ImageFileService imageFileService;

    @Override
    public Map<UUID, String> urlsFor(Collection<UUID> avatarImageIds) {
        // Lọc null: thumbnailUrls dùng Set.copyOf (ném NPE với null)
        Set<UUID> ids = avatarImageIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        // HashMap: Bên gọi get(null) cho người chưa có ảnh mà không bị NPE như Map.of()
        return ids.isEmpty() ? new HashMap<>() : new HashMap<>(imageFileService.thumbnailUrls(ids));
    }

    @Override
    public Optional<String> urlFor(UUID avatarImageId) {
        if (avatarImageId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(imageFileService.thumbnailUrls(Set.of(avatarImageId)).get(avatarImageId));
    }
}
