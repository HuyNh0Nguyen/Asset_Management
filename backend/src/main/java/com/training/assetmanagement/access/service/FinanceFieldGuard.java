package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.FinanceTarget;

import java.util.Collection;
import java.util.Set;

public interface FinanceFieldGuard {

    // Thiếu quyền một nhóm -> từ chối cả yêu cầu
    void checkWrite(FinanceTarget target, Set<String> presentFields, boolean canEditFinance, boolean canEditOther);

    // Sắp xếp / lọc theo trường [TC] mà không có ASSET_FINANCE_VIEW -> 403
    void checkSortOrFilter(FinanceTarget target, Collection<String> fields, ActorScope scope);
}
