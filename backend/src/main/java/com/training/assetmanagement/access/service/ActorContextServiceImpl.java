package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.access.domain.DataScope;
import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.access.domain.RoleGrants;
import com.training.assetmanagement.access.repository.GrantRow;
import com.training.assetmanagement.access.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ActorContextServiceImpl implements ActorContextService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public RoleGrants loadGrants(UUID userId) {
        Set<String> roleCodes = new LinkedHashSet<>();
        Map<Permission, DataScope> grants = new EnumMap<>(Permission.class);
        for (GrantRow row : roleRepository.findGrantRowsByUserId(userId)) {
            roleCodes.add(row.getRoleCode());
            if (row.getPermissionCode() != null) {
                grants.merge(Permission.valueOf(row.getPermissionCode()),
                    DataScope.valueOf(row.getScope()), DataScope::widest);
            }
        }
        return new RoleGrants(List.copyOf(roleCodes), Collections.unmodifiableMap(grants));
    }

    @Override
    @Transactional(readOnly = true)
    public ActorContext buildContext(UUID userId, String username, UUID orgUnitId, boolean mustChangePassword) {
        RoleGrants roleGrants = loadGrants(userId);
        return new ActorContext(userId, username, orgUnitId, roleGrants.roleCodes(),
            roleGrants.grants(), mustChangePassword);
    }
}
