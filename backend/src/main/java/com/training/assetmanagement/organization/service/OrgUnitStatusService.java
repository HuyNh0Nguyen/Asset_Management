package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgUnitBlocker;

import java.util.List;
import java.util.UUID;

public interface OrgUnitStatusService {

    // Giao dịch chỉ đọc, không khóa - dữ liệu để trình bày; lệnh thật kiểm lại dưới khóa
    List<OrgUnitBlocker> checkDeactivation(UUID id);

    // Không còn người dùng / đơn vị con / tài sản đang hoạt động
    void deactivate(UUID id);

    // Đi từ trên xuống: Cha phải ACTIVE
    void reactivate(UUID id);
}
