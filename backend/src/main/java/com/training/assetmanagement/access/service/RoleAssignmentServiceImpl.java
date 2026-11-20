package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.Role;
import com.training.assetmanagement.access.domain.RoleRef;
import com.training.assetmanagement.access.domain.UserRole;
import com.training.assetmanagement.access.repository.RoleRepository;
import com.training.assetmanagement.access.repository.UserRoleRepository;
import com.training.assetmanagement.shared.persistence.UuidV7;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleAssignmentServiceImpl implements RoleAssignmentService {

    private static final Comparator<RoleRef> BY_CODE = Comparator.comparing(RoleRef::code);

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CurrentActor currentActor;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<RoleRef> requireRoles(Collection<UUID> roleIds) {
        Set<UUID> ids = Set.copyOf(roleIds);
        List<Role> roles = roleRepository.findAllById(ids);
        if (roles.size() != ids.size()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return roles.stream().map(RoleAssignmentServiceImpl::toRef).sorted(BY_CODE).toList();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void assign(UUID userId, Collection<UUID> roleIds) {
        UUID actorId = currentActor.get().userId();
        LocalDateTime now = LocalDateTime.now(clock);
        userRoleRepository.saveAll(Set.copyOf(roleIds).stream()
            .map(roleId -> UserRole.create(UuidV7.generate(clock), userId, roleId, actorId, now))
            .toList());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replaceRoles(UUID userId, Collection<UUID> roleIds) {
        Set<UUID> wanted = Set.copyOf(roleIds);
        Set<UUID> current = userRoleRepository.findByUserId(userId).stream()
            .map(UserRole::getRoleId).collect(Collectors.toSet());
        Set<UUID> removed = new HashSet<>(current);
        removed.removeAll(wanted);
        Set<UUID> added = new HashSet<>(wanted);
        added.removeAll(current);
        if (!removed.isEmpty()) {
            userRoleRepository.deleteByUserIdAndRoleIds(userId, removed);
        }
        if (!added.isEmpty()) {
            assign(userId, added);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleRef> rolesOf(UUID userId) {
        return rolesOfUsers(List.of(userId)).getOrDefault(userId, List.of());
    }

    // Hai truy vấn cho cả trang - không N+1
    @Override
    @Transactional(readOnly = true)
    public Map<UUID, List<RoleRef>> rolesOfUsers(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<UserRole> links = userRoleRepository.findByUserIdIn(Set.copyOf(userIds));
        Set<UUID> roleIds = links.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        Map<UUID, RoleRef> roles = roleRepository.findAllById(roleIds).stream()
            .collect(Collectors.toMap(Role::getId, RoleAssignmentServiceImpl::toRef));
        Map<UUID, List<RoleRef>> result = new HashMap<>();
        for (UserRole link : links) {
            result.computeIfAbsent(link.getUserId(), key -> new ArrayList<>()).add(roles.get(link.getRoleId()));
        }
        result.values().forEach(list -> list.sort(BY_CODE));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleRef> rolesByCodes(Collection<String> codes) {
        if (codes.isEmpty()) {
            return List.of();
        }
        return roleRepository.findByCodeIn(codes).stream()
            .map(RoleAssignmentServiceImpl::toRef).sorted(BY_CODE).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> userIdsWithRole(UUID roleId) {
        return Set.copyOf(userRoleRepository.findUserIdsByRoleId(roleId));
    }

    private static RoleRef toRef(Role role) {
        return new RoleRef(role.getId(), role.getCode(), role.getName());
    }
}
