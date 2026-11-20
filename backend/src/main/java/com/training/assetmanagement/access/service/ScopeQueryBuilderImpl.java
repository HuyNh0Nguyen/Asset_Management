package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.organization.service.OrganizationLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScopeQueryBuilderImpl implements ScopeQueryBuilder {

    private final CurrentActor currentActor;
    private final OrganizationLookupService organizationLookupService;

    @Override
    public ActorScope forCurrentActor() {
        return forActor(currentActor.get());
    }

    // Nạp OrgTree đúng một lần cho cả thao tác
    @Override
    public ActorScope forActor(ActorContext actor) {
        return new ActorScope(actor, organizationLookupService.loadTree());
    }
}
