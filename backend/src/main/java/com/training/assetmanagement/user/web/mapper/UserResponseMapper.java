package com.training.assetmanagement.user.web.mapper;

import com.training.assetmanagement.access.domain.RoleRef;
import com.training.assetmanagement.user.domain.*;
import com.training.assetmanagement.user.web.dto.request.HeadReplacementRequest;
import com.training.assetmanagement.user.web.dto.response.*;

import java.util.List;

public final class UserResponseMapper {

    public static UserResponse toResponse(UserDetail detail) {
        User user = detail.user();
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
            user.getPhoneNumber(), detail.avatarUrl(), toUnit(detail.orgUnit()), toRank(detail.rank()),
            detail.roles().stream().map(UserResponseMapper::toRole).toList(), user.getStatus(),
            detail.allowedActions());
    }

    public static OrgUnitRefResponse toUnit(UnitRef unit) {
        return new OrgUnitRefResponse(unit.id(), unit.code(), unit.name(), unit.status());
    }

    public static RoleRefResponse toRole(RoleRef role) {
        return new RoleRefResponse(role.id(), role.code(), role.name());
    }

    public static HeadReplacementCheckResponse toUnitChangeCheck(List<UnitRef> units) {
        return new HeadReplacementCheckResponse(units.stream().map(UserResponseMapper::toUnit).toList());
    }

    public static UserDeactivationCheckResponse toDeactivationCheck(UserDeactivationCheck check) {
        return new UserDeactivationCheckResponse(
            check.blockers().stream().map(UserResponseMapper::toBlocker).toList(),
            check.headedUnits().stream().map(UserResponseMapper::toUnit).toList()
        );
    }

    public static List<HeadReplacement> toReplacements(List<HeadReplacementRequest> requests) {
        return requests.stream().map(HeadReplacementRequest::toReplacement).toList();
    }

    private static RankRefResponse toRank(RankRef rank) {
        return new RankRefResponse(rank.id(), rank.code(), rank.name());
    }

    private static UserBlockerResponse toBlocker(UserBlocker blocker) {
        return new UserBlockerResponse(blocker.kind(), blocker.total(), blocker.samples().stream()
            .map(item -> new UserBlockerItemResponse(item.id(), item.code(), item.name(), item.detail()))
            .toList());
    }
}
