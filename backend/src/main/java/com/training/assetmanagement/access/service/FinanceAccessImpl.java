package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.FinanceFields;
import com.training.assetmanagement.access.domain.FinanceTarget;
import com.training.assetmanagement.access.domain.Permission;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FinanceAccessImpl implements FinanceAccess {

    @Override
    public boolean canView(ActorScope scope, FinanceTarget target, UUID anchorUnitId, boolean relationGrant) {
        return switch (FinanceFields.anchorOf(target)) {
            case ANY_SCOPE -> scope.has(Permission.ASSET_FINANCE_VIEW);
            case MANAGING_UNIT -> scope.covers(Permission.ASSET_FINANCE_VIEW, anchorUnitId);
            case REQUESTER_UNIT -> relationGrant || scope.covers(Permission.ASSET_FINANCE_VIEW, anchorUnitId);
        };
    }

    @Override
    public boolean canEdit(ActorScope scope, FinanceTarget target, UUID anchorUnitId) {
        return switch (FinanceFields.anchorOf(target)) {
            case ANY_SCOPE -> scope.has(Permission.ASSET_FINANCE_EDIT);
            case MANAGING_UNIT, REQUESTER_UNIT -> scope.covers(Permission.ASSET_FINANCE_EDIT, anchorUnitId);
        };
    }

    @Override
    public <T> Specification<T> viewableSpec(ActorScope scope, ScopeBinding<T> binding) {
        return scope.actSpec(Permission.ASSET_FINANCE_VIEW, binding);
    }
}
