package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentActorImpl implements CurrentActor {

    @Override
    public ActorContext get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof  ActorContext actor) {
            return actor;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
