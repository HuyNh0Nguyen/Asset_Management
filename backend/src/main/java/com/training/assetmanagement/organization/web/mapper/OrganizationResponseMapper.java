package com.training.assetmanagement.organization.web.mapper;

import com.training.assetmanagement.organization.domain.*;
import com.training.assetmanagement.organization.web.dto.response.*;

import java.util.List;

// Đối tượng miền -> DTO; controller không tự dựng DTO, không trả entity
public final class OrganizationResponseMapper {

    public static OrgUnitNodeResponse toNode(OrgUnitView view) {
        return new OrgUnitNodeResponse(view.id(), view.code(), view.name(), view.unitType(), view.status(),
            view.parentId(), view.depth(), toHead(view.head()), view.childUnitType(), view.allowedActions());
    }

    public static OrgUnitHeadResponse toHead(OrgUnitHead head) {
        return head == null ? null
            : new OrgUnitHeadResponse(head.id(), head.username(), head.fullName(), head.orgUnitName(),
                head.avatarUrl());
    }

    public static MoveCheckResponse toMoveCheck(List<BlockingHead> blockingHeads) {
        return new MoveCheckResponse(blockingHeads.stream()
            .map(item -> new BlockingHeadResponse(item.unitId(), item.code(), item.name(), toHead(item.head())))
            .toList());
    }

    public static OrgUnitBlockersResponse toBlockers(List<OrgUnitBlocker> blockers) {
        return new OrgUnitBlockersResponse(blockers.stream()
            .map(blocker -> new OrgUnitBlockerResponse(blocker.kind(), blocker.total(),
                blocker.samples().stream().map(OrganizationResponseMapper::toBlockerItem).toList()))
            .toList());
    }

    public static RankResponse toRank(Rank rank) {
        return new RankResponse(rank.getId(), rank.getCode(), rank.getName(), rank.getSeniority());
    }

    private static OrgUnitBlockerItemResponse toBlockerItem(OrgUnitBlockerItem item) {
        return new OrgUnitBlockerItemResponse(item.id(), item.code(), item.name(), item.inactive());
    }

    private OrganizationResponseMapper() {}
}
