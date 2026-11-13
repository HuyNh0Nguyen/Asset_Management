package com.training.assetmanagement.user.service;

import com.training.assetmanagement.user.domain.MeProfile;

public interface MeService {

    // /api/me bản tối thiểu - chưa có ảnh đại diện
    MeProfile getMe();
}
