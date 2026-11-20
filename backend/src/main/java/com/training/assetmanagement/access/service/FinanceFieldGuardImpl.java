package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.FinanceFields;
import com.training.assetmanagement.access.domain.FinanceTarget;
import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class FinanceFieldGuardImpl implements FinanceFieldGuard {

    // Mang trường [TC] mà không sửa được tài chính, hoặc trường nhóm khác mà không sửa được nhóm đó
    @Override
    public void checkWrite(FinanceTarget target, Set<String> presentFields, boolean canEditFinance,
                           boolean canEditOther) {
        boolean hasFinance = presentFields.stream().anyMatch(field -> FinanceFields.isFinance(target, field));
        boolean hasOther = presentFields.stream().anyMatch(field -> !FinanceFields.isFinance(target, field));
        if ((hasFinance && !canEditFinance) || (hasOther && !canEditOther)) {
            throw new BusinessException(ErrorCode.FIELD_GROUP_FORBIDDEN);
        }
    }

    // Có quyền thì bên gọi AND thêm FinanceAccess.viewableSpec để thu hẹp dòng
    @Override
    public void checkSortOrFilter(FinanceTarget target, Collection<String> fields, ActorScope scope) {
        boolean touchesFinance = fields.stream().anyMatch(field -> FinanceFields.isFinance(target, field));
        if (touchesFinance && !scope.has(Permission.ASSET_FINANCE_VIEW)) {
            throw new BusinessException(ErrorCode.FIELD_GROUP_FORBIDDEN);
        }
    }
}
