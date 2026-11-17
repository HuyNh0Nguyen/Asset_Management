package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgUserRef;
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

// Chỉ tiêm UserRepository - không tiêm service của organization, tránh vòng bean
@Service
@RequiredArgsConstructor
public class OrgUnitMemberDirectoryImpl implements OrgUnitMemberDirectory {

    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Map<UUID, OrgUserRef> lockForShare(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return toRefs(userRepository.findAllByIdForShare(Set.copyOf(userIds)));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, OrgUserRef> findByIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return toRefs(userRepository.findAllById(Set.copyOf(userIds)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUserRef> activeUsersIn(Collection<UUID> unitIds) {
        if (unitIds.isEmpty()) {
            return List.of();
        }
        return userRepository.findByOrgUnitIdInAndStatusOrderByFullNameAsc(Set.copyOf(unitIds), UserStatus.ACTIVE)
            .stream()
            .map(OrgUnitMemberDirectoryImpl::toRef)
            .toList();
    }

    private static Map<UUID, OrgUserRef> toRefs(List<User> users) {
        return users.stream()
            .map(OrgUnitMemberDirectoryImpl::toRef)
            .collect(Collectors.toMap(OrgUserRef::id, Function.identity()));
    }

    private static OrgUserRef toRef(User user) {
        return new OrgUserRef(user.getId(), user.getUsername(), user.getFullName(), user.getOrgUnitId(),
            user.getStatus() == UserStatus.ACTIVE);
    }
}
