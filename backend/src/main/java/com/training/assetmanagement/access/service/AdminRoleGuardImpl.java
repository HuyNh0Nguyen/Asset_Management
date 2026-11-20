package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.Role;
import com.training.assetmanagement.access.domain.RoleCode;
import com.training.assetmanagement.access.repository.RoleRepository;
import com.training.assetmanagement.access.repository.UserRoleRepository;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminRoleGuardImpl implements AdminRoleGuard {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AccessMemberDirectory accessMemberDirectory;

    // Khóa đúng một dòng nhờ uk_roles_code; không xét trước đích có phải Admin
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID lockAdminRole() {
        return roleRepository.findByCodeForUpdate(RoleCode.ADMIN.name())
            .map(Role::getId)
            .orElseThrow(() -> new IllegalStateException("System role ADMIN is missing"));
    }

    // Tập Admin ACTIVE \ {đích} rỗng -> chặn; tự thao tác trên chính mình đi cùng phép kiểm
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void checkStillHasActiveAdmin(UUID targetUserId) {
        Set<UUID> others = new HashSet<>(activeAdmins());
        others.remove(targetUserId);
        if (others.isEmpty()) {
            throw new BusinessException(ErrorCode.LAST_ACTIVE_ADMIN);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean holdsAdmin(UUID userId) {
        return adminHolders().contains(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLastActiveAdmin(UUID userId) {
        Set<UUID> active = activeAdmins();
        return active.contains(userId) && active.size() == 1;
    }

    private Set<UUID> activeAdmins() {
        return accessMemberDirectory.activeOrgUnits(adminHolders()).keySet();
    }

    private Set<UUID> adminHolders() {
        UUID adminRoleId = roleRepository.findByCode(RoleCode.ADMIN.name())
            .map(Role::getId)
            .orElseThrow(() -> new IllegalStateException("System role ADMIN is missing"));
        return Set.copyOf(userRoleRepository.findUserIdsByRoleId(adminRoleId));
    }
}
