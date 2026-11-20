package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.domain.RoleRef;
import com.training.assetmanagement.access.service.RoleAssignmentService;
import com.training.assetmanagement.organization.domain.OrgNode;
import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.organization.domain.Rank;
import com.training.assetmanagement.organization.service.OrganizationLookupService;
import com.training.assetmanagement.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

// Ghép UserDetail không N+1: Vai trò một lần, đơn vị từ OrgTree, cấp bậc một lần, URL ảnh theo lô
@Component
@RequiredArgsConstructor
public class UserDetailAssembler {

    private final RoleAssignmentService roleAssignmentService;
    private final OrganizationLookupService organizationLookupService;
    private final AvatarUrlResolver avatarUrlResolver;

    public List<UserDetail> assemble(List<User> users, OrgTree tree) {
        if (users.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<RoleRef>> roles = roleAssignmentService.rolesOfUsers(users.stream().map(User::getId).toList());
        Map<UUID, RankRef> ranks = organizationLookupService.listRanks().stream()
            .collect(Collectors.toMap(Rank::getId, rank -> new RankRef(rank.getId(), rank.getCode(), rank.getName())));
        Map<UUID, String> avatars = avatarUrlResolver.urlsFor(users.stream()
            .map(User::getAvatarImageId).filter(Objects::nonNull).toList());
        return users.stream()
            .map(user -> new UserDetail(user, unitRef(tree, user.getOrgUnitId()), ranks.get(user.getRankId()),
                roles.getOrDefault(user.getId(), List.of()),
                user.getAvatarImageId() == null ? null : avatars.get(user.getAvatarImageId()),
                UserRules.allowedActions(user.getStatus(), user.getAvatarImageId() != null)))
            .toList();
    }

    public UserDetail assemble(User user, OrgTree tree) {
        return assemble(List.of(user), tree).get(0);
    }

    public static UnitRef unitRef(OrgTree tree, UUID unitId) {
        OrgNode node = tree.find(unitId)
            .orElseThrow(() -> new IllegalStateException("Org unit " + unitId + " is missing from tree"));
        return new UnitRef(node.id(), node.code(), node.name(), node.status());
    }
}
