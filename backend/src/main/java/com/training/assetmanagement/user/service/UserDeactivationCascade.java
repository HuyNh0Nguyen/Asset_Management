package com.training.assetmanagement.user.service;

import java.util.UUID;

// TODO: Phase 6 thêm RequestDeactivationCascade: Hủy phiếu, gỡ task_candidates - cùng giao dịch, cùng actor
public interface UserDeactivationCascade {

    void onUserDeactivated(UUID userId);
}
