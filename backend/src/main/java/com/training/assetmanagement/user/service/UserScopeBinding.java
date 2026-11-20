package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.service.ScopeBinding;
import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.domain.UserAttribute;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

// Đơn vị = users.org_unit_id; SELF = chính mình, áp cả xem lẫn tác động
@Component
public class UserScopeBinding implements ScopeBinding<User> {

    @Override
    public Expression<UUID> managingUnit(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return root.get(UserAttribute.ORG_UNIT_ID.getPath());
    }

    @Override
    public Optional<Predicate> self(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb, UUID userId) {
        return Optional.of(cb.equal(root.get(UserAttribute.ID.getPath()), userId));
    }

    @Override
    public boolean selfAppliesToAct() {
        return true;
    }
}
