package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.service.AccessMemberDirectory;
import com.training.assetmanagement.user.domain.UserStatus;
import com.training.assetmanagement.user.repository.UserRepository;
import com.training.assetmanagement.user.repository.UserUnitRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// Chỉ tiêm UserRepository - tránh vòng bean với access
@Service
@RequiredArgsConstructor
public class AccessMemberDirectoryImpl implements AccessMemberDirectory {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, UUID> activeOrgUnits(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findUnitRows(Set.copyOf(userIds), UserStatus.ACTIVE).stream()
            .collect(Collectors.toMap(UserUnitRow::userId, UserUnitRow::orgUnitId));
    }
}
