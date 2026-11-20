package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;
import com.training.assetmanagement.audit.domain.AuditTargetType;
import com.training.assetmanagement.organization.domain.*;
import com.training.assetmanagement.organization.repository.OrgUnitRepository;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.training.assetmanagement.organization.service.OrgUnitGuards.ensure;
import static com.training.assetmanagement.organization.service.OrgUnitGuards.requireNode;

@Service
@RequiredArgsConstructor
public class OrgUnitStatusServiceImpl implements OrgUnitStatusService {

    private final OrgUnitRepository orgUnitRepository;
    private final OrgTreeService orgTreeService;
    private final OrgUnitBlockerService blockerService;
    private final OrgAuditService orgAuditService;
    private final CurrentActor currentActor;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitBlocker> checkDeactivation(UUID id) {
        OrgTree tree = orgTreeService.load();
        ensure(OrgUnitRules.check(OrgUnitAction.DEACTIVATE, requireNode(tree, id), tree, true));
        return blockerService.deactivationBlockers(tree, id);
    }

    // Khóa ghi một dòng là đủ: Mọi đường ghi chọn đơn vị này làm đích đều khóa đọc dòng đó trước
    @Override
    @Transactional
    public void deactivate(UUID id) {
        // Câu đầu tiên của giao dịch
        orgUnitRepository.findByIdForUpdate(id).orElseThrow(OrgUnitGuards::notFound);
        OrgTree tree = orgTreeService.load();
        OrgNode node = requireNode(tree, id);
        ensure(OrgUnitRules.check(OrgUnitAction.DEACTIVATE, node, tree, true));
        if (!blockerService.deactivationBlockers(tree, id).isEmpty()) {
            throw new BusinessException(ErrorCode.ORG_UNIT_NOT_EMPTY);
        }
        // Không kiểm trưởng: Cây con không còn ai ACTIVE nên trưởng tất yếu trống
        changeStatus(node, OrgUnitStatus.ACTIVE, OrgUnitStatus.INACTIVE, AuditAction.DEACTIVATE);
    }

    @Override
    @Transactional
    public void reactivate(UUID id) {
        OrgUnit unit = orgUnitRepository.findByIdForUpdate(id).orElseThrow(OrgUnitGuards::notFound);
        // Khóa đọc cha: Va với ngừng hoạt động của cha
        if (unit.getParentId() != null) {
            orgUnitRepository.findByIdForShare(unit.getParentId());
        }
        OrgTree tree = orgTreeService.load();
        OrgNode node = requireNode(tree, id);
        ensure(OrgUnitRules.check(OrgUnitAction.REACTIVATE, node, tree, true));
        // Trở lại không người, không trưởng - Admin gán lại qua hàng Người dùng và Sửa
        changeStatus(node, OrgUnitStatus.INACTIVE, OrgUnitStatus.ACTIVE, AuditAction.REACTIVATE);
    }

    private void changeStatus(OrgNode node, OrgUnitStatus current, OrgUnitStatus next, AuditAction action) {
        int updated = orgUnitRepository.updateStatus(node.id(), current, next,
            currentActor.get().userId(), LocalDateTime.now(clock));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Org unit " + node.id() + " status changed concurrently");
        }
        AuditChanges changes = new AuditChanges().track(OrgAuditField.STATUS, current, next);
        orgAuditService.log(action, AuditTargetType.ORG_UNIT, node.id(), node.code(), changes);
    }
}
