package com.training.assetmanagement.organization.domain;

// Nhóm vướng khi ngừng hoạt động (ACTIVE_*, LIVE_*) hoặc xóa (còn lại).
// Thứ tự khai báo = thứ tự hiển thị
public enum OrgUnitBlockerKind {
    ACTIVE_USER,
    USER,
    ACTIVE_CHILD_UNIT,
    CHILD_UNIT,
    LIVE_ASSET,
    ASSET,
    LIVE_BATCH,
    BATCH
}
