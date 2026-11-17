package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgNode;
import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;

import java.util.Optional;
import java.util.UUID;

// Đổi kết quả quy tắc thần thành ngoại lệ - dùng chung trong service của miền tổ chức
final class OrgUnitGuards {

    static void ensure(Optional<ErrorCode> violation) {
        if (violation.isPresent()) {
            throw new BusinessException(violation.get());
        }
    }

    static OrgNode requireNode(OrgTree tree, UUID id) {
        return tree.find(id).orElseThrow(OrgUnitGuards::notFound);
    }

    static BusinessException notFound() {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    private OrgUnitGuards() {}
}
