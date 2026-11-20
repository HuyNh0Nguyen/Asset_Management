package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.ActorContext;

// Lệnh ghi lấy ActorScope SAU khóa cuối cùng
public interface ScopeQueryBuilder {

    ActorScope forCurrentActor();

    ActorScope forActor(ActorContext actor);
}
