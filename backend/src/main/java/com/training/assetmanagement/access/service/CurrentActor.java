package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.ActorContext;

// Người thao tác của request hiện tại
public interface CurrentActor {

    // Không có phiên -> 401 UNAUTHORIZED
    ActorContext get();
}
