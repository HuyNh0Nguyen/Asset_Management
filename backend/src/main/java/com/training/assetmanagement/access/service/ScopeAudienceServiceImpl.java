package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.DataScope;
import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.access.domain.ScopeResolver;
import com.training.assetmanagement.access.domain.UnitCoverage;
import com.training.assetmanagement.access.repository.HolderGrant;
import com.training.assetmanagement.access.repository.RolePermissionRepository;
import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.organization.service.OrganizationLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

// Ba truy vấn nhỏ, phần còn lại trong bộ nhớ. Dùng chung ScopeResolver với chiều xuôi -
// tự viết điều kiện SQL riêng là nguồn sự thật thứ hai cho phạm vi
@Service
@RequiredArgsConstructor
public class ScopeAudienceServiceImpl implements ScopeAudienceService {

    private final RolePermissionRepository rolePermissionRepository;
    private final AccessMemberDirectory accessMemberDirectory;
    private final OrganizationLookupService organizationLookupService;

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> activeUsersCovering(Permission permission, UUID unitId) {
        return activeUsersCovering(permission, List.of(unitId)).getOrDefault(unitId, Set.of());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Set<UUID>> activeUsersCovering(Permission permission, Collection<UUID> unitIds) {
        Map<UUID, UnitCoverage> coverages = activeCoverages(permission);
        Map<UUID, Set<UUID>> result = new HashMap<>();
        for (UUID unitId : Set.copyOf(unitIds)) {
            Set<UUID> users = new HashSet<>();
            coverages.forEach((userId, coverage) -> {
                if (coverage.covers(unitId)) {
                    users.add(userId);
                }
            });
            result.put(unitId, Set.copyOf(users));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> activeHolders(Permission permission) {
        Map<UUID, DataScope> scopes = widestScopes(permission);
        return scopes.isEmpty() ? Set.of() : Set.copyOf(accessMemberDirectory.activeOrgUnits(scopes.keySet()).keySet());
    }

    // SELF không phủ đơn vị nào -> người chỉ có SELF không bao giờ là người nhận theo đơn vị
    private Map<UUID, UnitCoverage> activeCoverages(Permission permission) {
        Map<UUID, DataScope> scopes = widestScopes(permission);
        if (scopes.isEmpty()) {
            return Map.of();
        }
        // Bước 2: Chỉ người ACTIVE, kèm đơn vị
        Map<UUID, UUID> units = accessMemberDirectory.activeOrgUnits(scopes.keySet());
        if (units.isEmpty()) {
            return Map.of();
        }
        // Bước 3: Cùng ScopeResolver của chiều xuôi
        OrgTree tree = organizationLookupService.loadTree();
        Map<UUID, UnitCoverage> result = new HashMap<>();
        units.forEach((userId, unitId) ->
            result.put(userId, ScopeResolver.resolve(scopes.get(userId), userId, unitId, tree)));
        return result;
    }

    // Bước 1: Hợp qua các vai trò = phạm vi rộng nhất
    private Map<UUID, DataScope> widestScopes(Permission permission) {
        Map<UUID, DataScope> scopes = new HashMap<>();
        for (HolderGrant grant : rolePermissionRepository.findHolderGrants(permission)) {
            scopes.merge(grant.userId(), grant.scope(), DataScope::widest);
        }
        return scopes;
    }
}
