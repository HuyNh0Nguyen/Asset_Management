package com.training.assetmanagement.user.service;

import com.training.assetmanagement.user.domain.NewUser;
import com.training.assetmanagement.user.domain.UnitRef;
import com.training.assetmanagement.user.domain.UserDetail;
import com.training.assetmanagement.user.domain.UserProfileUpdate;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDetail create(NewUser command);

    UserDetail update(UUID id, UserProfileUpdate command);

    // Đơn vị cần chọn trưởng thay khi đổi sang orgUnitId - chỉ để trình bày
    List<UnitRef> checkUnitChange(UUID id, UUID orgUnitId);
}
