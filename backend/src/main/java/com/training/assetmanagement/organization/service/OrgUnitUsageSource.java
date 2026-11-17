package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgUnitBlocker;

import java.util.List;
import java.util.UUID;

// Cổng: Miền CHỦ bảng tham chiếu tới org_units tự đếm vướng của mình.
// organization không đọc bảng của miền khác và không phụ thuộc ngược vào user / asset
public interface OrgUnitUsageSource {

    // Chặn ngừng hoạt động - chỉ trả nhóm có total > 0
    List<OrgUnitBlocker> deactivationBlockers(UUID unitId);

    // Chặn xóa - chỉ trả nhóm có total > 0
    List<OrgUnitBlocker> deletionBlockers(UUID unitId);
}
