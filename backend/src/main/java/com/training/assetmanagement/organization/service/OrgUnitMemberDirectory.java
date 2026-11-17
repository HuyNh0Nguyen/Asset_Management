package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgUserRef;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Cổng đọc người dùng cho miền tổ chức - miền user hiện thực
public interface OrgUnitMemberDirectory {

    // Khóa đọc dòng người dùng theo id tăng dần - va với khóa ghi khi đổi đơn vị / vô hiệu hóa.
    // Phải gọi trong giao dịch đang mở
    Map<UUID, OrgUserRef> lockForShare(Collection<UUID> userIds);

    // Không khóa - cho endpoint kiểm và tên hiển thị
    Map<UUID, OrgUserRef> findByIds(Collection<UUID> userIds);

    // Người dùng ACTIVE thuộc các đơn vị - ứng viên trưởng đơn vị
    List<OrgUserRef> activeUsersIn(Collection<UUID> unitIds);
}
