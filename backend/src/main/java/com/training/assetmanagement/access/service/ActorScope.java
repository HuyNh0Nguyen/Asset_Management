package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.*;
import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

// Phạm vi của một người cho cả thao tác - gắn đúng một OrgTree, không memo ẩn qua request
@Getter
public final class ActorScope {

    private final ActorContext actor;
    private final OrgTree tree;
    private final Map<Permission, UnitCoverage> coverages = new EnumMap<>(Permission.class);

    public ActorScope(ActorContext actor, OrgTree tree) {
        this.actor = actor;
        this.tree = tree;
    }

    public boolean has(Permission permission) {
        return actor.hasPermission(permission);
    }

    public UnitCoverage coverage(Permission permission) {
        return coverages.computeIfAbsent(permission, key -> actor.scopeOf(key)
            .map(scope -> ScopeResolver.resolve(scope, actor.userId(), actor.orgUnitId(), tree))
            .orElse(UnitCoverage.NONE));
    }

    public boolean covers(Permission permission, UUID unitId) {
        return coverage(permission).covers(unitId);
    }

    // Xem: managingUnit IN units ∨ usingUnitIn(units) ∨ self(user)
    public <T> Specification<T> viewSpec(Permission permission, ScopeBinding<T> binding) {
        UnitCoverage coverage = coverage(permission);
        return (root, query, cb) -> {
            if (coverage.all()) {
                return cb.conjunction();
            }
            List<Predicate> any = new ArrayList<>();
            if (!coverage.unitIds().isEmpty()) {
                any.add(binding.managingUnit(root, query, cb).in(coverage.unitIds()));
                binding.usingUnitIn(root, query, cb, coverage.unitIds()).ifPresent(any::add);
            }
            if (coverage.selfUserId() != null) {
                binding.self(root, query, cb, coverage.selfUserId()).ifPresent(any::add);
            }
            return anyOf(cb, any);
        };
    }

    // Tác động: managingUnit IN units ∨ (selfAppliesToAct ∧ self(user))
    public <T> Specification<T> actSpec(Permission permission, ScopeBinding<T> binding) {
        UnitCoverage coverage = coverage(permission);
        return (root, query, cb) -> {
            if (coverage.all()) {
                return cb.conjunction();
            }
            List<Predicate> any = new ArrayList<>();
            if (!coverage.unitIds().isEmpty()) {
                any.add(binding.managingUnit(root, query, cb).in(coverage.unitIds()));
            }
            if (binding.selfAppliesToAct() && coverage.selfUserId() != null) {
                binding.self(root, query, cb, coverage.selfUserId()).ifPresent(any::add);
            }
            return anyOf(cb, any);
        };
    }

    // Hàm duy nhất ra ba kết cục - allowedActions và lệnh thật cùng gọi
    public ScopeDecision decide(Permission view, Permission act, ScopeTarget target) {
        if (!coverage(view).matchesView(target)) {
            return ScopeDecision.NOT_VISIBLE;
        }
        return coverage(act).matchesAct(target) ? ScopeDecision.ALLOWED : ScopeDecision.VISIBLE_ONLY;
    }

    public void require(Permission view, Permission act, ScopeTarget target) {
        switch (decide(view, act, target)) {
            case NOT_VISIBLE -> throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
            case VISIBLE_ONLY -> throw new BusinessException(ErrorCode.FORBIDDEN);
            case ALLOWED -> { }
        }
    }

    // Rỗng -> không dòng nào; không bao giờ sinh IN ()
    private static Predicate anyOf(CriteriaBuilder cb, List<Predicate> predicates) {
        return predicates.isEmpty() ? cb.disjunction() : cb.or(predicates.toArray(Predicate[]::new));
    }
}
