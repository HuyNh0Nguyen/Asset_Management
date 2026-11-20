package com.training.assetmanagement.user.service;

import com.training.assetmanagement.organization.domain.OrgUserRef;
import com.training.assetmanagement.organization.service.OrgUnitMemberDirectory;
import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.domain.UserStatus;
import com.training.assetmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// Không tiêm service của organization, tránh vòng bean
@Service
@RequiredArgsConstructor
public class OrgUnitMemberDirectoryImpl implements OrgUnitMemberDirectory {

    private final UserRepository userRepository;
    private final AvatarUrlResolver avatarUrlResolver;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Map<UUID, OrgUserRef> lockForShare(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return toRefMap(userRepository.findAllByIdForShare(Set.copyOf(userIds)));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, OrgUserRef> findByIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return toRefMap(userRepository.findAllById(Set.copyOf(userIds)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUserRef> activeUsersIn(Collection<UUID> unitIds) {
        if (unitIds.isEmpty()) {
            return List.of();
        }
        return toRefs(userRepository.findByOrgUnitIdInAndStatusOrderByFullNameAsc(Set.copyOf(unitIds), UserStatus.ACTIVE));
    }

    private Map<UUID, OrgUserRef> toRefMap(List<User> users) {
        return toRefs(users).stream().collect(Collectors.toMap(OrgUserRef::id, Function.identity()));
    }

    // URL ảnh theo lô - một truy vấn image_files
    private List<OrgUserRef> toRefs(List<User> users) {
        Map<UUID, String> avatars = avatarUrlResolver.urlsFor(users.stream()
            .map(User::getAvatarImageId).filter(Objects::nonNull).toList());
        return users.stream()
            .map(user -> new OrgUserRef(user.getId(), user.getUsername(), user.getFullName(), user.getOrgUnitId(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getAvatarImageId() == null ? null : avatars.get(user.getAvatarImageId())))
            .toList();
    }
}
