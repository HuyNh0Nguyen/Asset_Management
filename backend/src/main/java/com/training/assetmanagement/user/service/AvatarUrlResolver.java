package com.training.assetmanagement.user.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Cổng giữa miền user và media - ai thấy tên người dùng thì thấy ảnh
public interface AvatarUrlResolver {

    // Một truy vấn cho cả lô; id null bị bỏ qua; get(null) trả null
    Map<UUID, String> urlsFor(Collection<UUID> avatarImageIds);

    Optional<String> urlFor(UUID avatarImageId);
}
