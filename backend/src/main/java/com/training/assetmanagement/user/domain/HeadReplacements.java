package com.training.assetmanagement.user.domain;

import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;

import java.util.*;

// Quy tắc thuần cho danh sách trưởng thay thế
public final class HeadReplacements {

    // Đơn vị đang làm trưởng mà đơn vị mới không nằm trong cây con của nó
    public static List<UUID> mustReplace(Collection<UUID> headedUnitIds, UUID newOrgUnitId, OrgTree tree) {
        return headedUnitIds.stream()
            .filter(unitId -> !tree.isInSubtree(newOrgUnitId, unitId))
            .sorted()
            .toList();
    }

    // Tập khai phải bằng đúng tập cần thay - thiếu / thừa / trùng -> 422; trả theo id tăng dần
    public static Map<UUID, UUID> match(Collection<UUID> mustReplace, List<HeadReplacement> requested) {
        Map<UUID, UUID> result = new TreeMap<>();
        for (HeadReplacement replacement : requested) {
            if (result.containsKey(replacement.unitId())) {
                throw new BusinessException(ErrorCode.HEAD_REPLACEMENT_MISMATCH);
            }
            result.put(replacement.unitId(), replacement.newHeadUserId());
        }
        if (!result.keySet().equals(Set.copyOf(mustReplace))) {
            throw new BusinessException(ErrorCode.HEAD_REPLACEMENT_MISMATCH);
        }
        return result;
    }

    private HeadReplacements() {}
}
