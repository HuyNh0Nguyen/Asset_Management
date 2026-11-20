package com.training.assetmanagement.user.service;

import com.training.assetmanagement.user.domain.HeadReplacement;
import com.training.assetmanagement.user.domain.UserDeactivationCheck;

import java.util.List;
import java.util.UUID;

public interface DeactivationService {

    UserDeactivationCheck check(UUID id);

    void deactivate(UUID id, List<HeadReplacement> headReplacements);

    void reactivate(UUID id, UUID orgUnitId);
}
