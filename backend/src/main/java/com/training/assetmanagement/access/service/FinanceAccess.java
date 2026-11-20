package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.FinanceTarget;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

// TODO: Quy tắc đơn vị của nhóm Tài chính - dưng ở phase 2, dùng từ phase 3
public interface FinanceAccess {

    // relationGrant: ngoại lệ do workflow tính theo cặp (người dùng, phiếu)
    boolean canView(ActorScope scope, FinanceTarget target, UUID anchorUnitId, boolean relationGrant);

    boolean canEdit(ActorScope scope, FinanceTarget target, UUID anchorUnitId);

    // = actSpec(ASSET_FINANCE_VIEW): Tài chính so đơn vị quản lý, không so đơn vị sử dụng
    <T> Specification<T> viewableSpec(ActorScope scope, ScopeBinding<T> binding);
}
