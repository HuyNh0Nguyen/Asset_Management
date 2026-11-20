package com.training.assetmanagement.user.service;

import com.training.assetmanagement.user.domain.UserBlocker;

import java.util.List;
import java.util.UUID;

// Rỗng = không chặn. Chạy trong giao dịch của bên gọi (kiểm lại dưới khóa ở lệnh thật)
public interface UserDeactivationBlockerSource {

    List<UserBlocker> deactivationBlockers(UUID userId);
}
