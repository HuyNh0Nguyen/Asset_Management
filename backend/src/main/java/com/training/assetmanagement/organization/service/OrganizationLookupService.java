package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.organization.domain.OrgUnit;
import com.training.assetmanagement.organization.domain.OrgUnitType;
import com.training.assetmanagement.organization.domain.Rank;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// Cửa DUY NHẤT của miền khác vào org_units / ranks
public interface OrganizationLookupService {

    Optional<OrgUnit> findOrgUnit(UUID orgUnitId);

    Optional<Rank> findRank(UUID rankId);

    // ScopeQueryBuilder (access), ApproverResolver (TODO phase 5) - gồm cả nút INACTIVE
    OrgTree loadTree();

    // Chọn đơn vị đích khi ghi (user, asset, lifecycle): Khóa đọc trong giao dịch của bên gọi.
    // Không có -> 404; INACTIVE -> ORG_UNIT_INACTIVE; sai loại -> ORG_UNIT_TYPE_NOT_ALLOWED
    OrgUnit requireActiveUnit(UUID unitId, Set<OrgUnitType> allowedTypes);

    Rank requireRank(UUID rankId);

    // Đơn vị mà người dùng đang làm trưởng
    List<UUID> unitsHeadedBy(UUID userId);

    // Đổi / gỡ trưởng; ghi org_units vẫn do miền chủ
    void replaceHead(UUID unitId, UUID newHeadUserId);

    // MANDATORY: Khóa ghi đơn vị đang làm trưởng - va với Sửa đơn vị
    List<OrgUnit> lockUnitsHeadedBy(UUID userId);

    // Cấp bậc chỉ vài dòng - danh sách người dùng tra trong bộ nhớ
    List<Rank> listRanks();
}
