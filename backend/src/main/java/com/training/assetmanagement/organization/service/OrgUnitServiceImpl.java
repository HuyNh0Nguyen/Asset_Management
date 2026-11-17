package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditTargetType;
import com.training.assetmanagement.organization.domain.*;
import com.training.assetmanagement.organization.repository.OrgUnitRepository;
import com.training.assetmanagement.shared.persistence.ConstraintViolations;
import com.training.assetmanagement.shared.persistence.UuidV7;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.training.assetmanagement.organization.service.OrgUnitGuards.ensure;
import static com.training.assetmanagement.organization.service.OrgUnitGuards.requireNode;

// Mọi lệnh ghi có câu đầu tiên chạm CSDL là câu khóa; OrgTree nạp SAU khóa, đúng một lần
@Service
@RequiredArgsConstructor
public class OrgUnitServiceImpl implements OrgUnitService {

    private final OrgUnitRepository orgUnitRepository;
    private final OrgTreeService orgTreeService;
    private final OrgRootLocator orgRootLocator;
    private final OrgUnitBlockerService blockerService;
    private final OrgUnitMemberDirectory memberDirectory;
    private final OrgAuditService orgAuditService;
    private final CurrentActor currentActor;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitView> getTree() {
        boolean canManage = currentActor.get().hasPermission(Permission.ORG_MANAGE);
        OrgTree tree = orgTreeService.load();
        Set<UUID> headIds = tree.nodes().stream()
            .map(OrgNode::headUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<UUID, OrgUnitHead> heads = headsOf(tree, memberDirectory.findByIds(headIds).values());
        return tree.preOrder().stream()
            .map(entry -> toView(tree, entry.node(), entry.depth(), heads, canManage))
            .toList();
    }

    @Override
    @Transactional
    public OrgUnitView create(String code, String name, OrgUnitType unitType, UUID parentId) {
        // Kiểm trước mọi khóa
        ensure(OrgUnitRules.checkCreatableType(unitType));
        // Khóa đọc dòng cha - câu đầu tiên chạm CSDL
        orgUnitRepository.findByIdForShare(parentId).orElseThrow(OrgUnitGuards::notFound);
        OrgTree tree = orgTreeService.load();
        OrgNode parent = requireNode(tree, parentId);
        // Cha ACTIVE - cùng hàm với allowedActions của nút cha
        ensure(OrgUnitRules.check(OrgUnitAction.ADD_CHILD, parent, tree, true));
        ensure(OrgUnitRules.checkParentType(unitType, parent.unitType()));
        if (orgUnitRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.ORG_UNIT_CODE_DUPLICATE);
        }

        OrgUnit unit = OrgUnit.create(UuidV7.generate(clock), code, name.strip(), unitType, parentId,
            currentActor.get().userId(), LocalDateTime.now(clock));
        try {
            orgUnitRepository.saveAndFlush(unit);
        } catch (DataIntegrityViolationException ex) {
            // Hai Admin tạo cùng mã cùng lúc
            if (ConstraintViolations.violates(ex, OrgConstraint.UK_ORG_UNITS_CODE.getConstraintName())) {
                throw new BusinessException(ErrorCode.ORG_UNIT_CODE_DUPLICATE);
            }
            throw ex;
        }

        OrgAuditChanges changes = new OrgAuditChanges()
            .putNew(OrgAuditField.CODE, unit.getCode())
            .putNew(OrgAuditField.NAME, unit.getName())
            .putNew(OrgAuditField.UNIT_TYPE, unit.getUnitType())
            .putNew(OrgAuditField.PARENT_ID, unit.getParentId())
            .putNew(OrgAuditField.STATUS, unit.getStatus());
        orgAuditService.log(AuditAction.CREATE, AuditTargetType.ORG_UNIT, unit.getId(), unit.getCode(), changes);
        return toView(tree, unit.toNode(), tree.depthOf(parentId) + 1, Map.of(), true);
    }

    @Override
    @Transactional
    public OrgUnitView update(UUID id, String name, UUID headUserId) {
        // Khóa ghi đơn vị - câu đầu tiên
        OrgUnit unit = orgUnitRepository.findByIdForUpdate(id).orElseThrow(OrgUnitGuards::notFound);
        OrgTree tree = applyChanges(unit, name.strip(), headUserId);
        Map<UUID, OrgUnitHead> heads = headUserId == null
            ? Map.of()
            : headsOf(tree, memberDirectory.findByIds(Set.of(headUserId)).values());
        return toView(tree, unit.toNode(), tree.depthOf(id), heads, true);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replaceHead(UUID unitId, UUID headUserId) {
        OrgUnit unit = orgUnitRepository.findByIdForUpdate(unitId).orElseThrow(OrgUnitGuards::notFound);
        applyChanges(unit, unit.getName(), headUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitHead> headCandidates(UUID id) {
        OrgTree tree = orgTreeService.load();
        requireNode(tree, id);
        return memberDirectory.activeUsersIn(tree.subtreeIds(id)).stream()
            .map(user -> toHead(tree, user))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockingHead> checkMove(UUID id, UUID parentId) {
        OrgTree tree = orgTreeService.load();
        OrgNode node = requireNode(tree, id);
        validateMove(tree, node, requireNode(tree, parentId));
        if (Objects.equals(node.parentId(), parentId)) {
            return List.of();
        }

        // Trưởng của tổ tiên cũ đọc từ cây, chỗ ngồi đọc không khóa - chỉ để trình bày
        Map<UUID, UUID> headByUnit = new LinkedHashMap<>();
        for (UUID ancestorId : OrgUnitRules.detachedAncestorIds(tree, id, parentId)) {
            UUID headId = requireNode(tree, ancestorId).headUserId();
            if (headId != null) {
                headByUnit.put(ancestorId, headId);
            }
        }
        Map<UUID, OrgUserRef> seats = memberDirectory.findByIds(headByUnit.values());
        Map<UUID, OrgUnitHead> heads = headsOf(tree, seats.values());
        return OrgUnitRules.unitsWithDetachedHead(headByUnit, seatsByUser(seats), tree.subtreeIds(id)).stream()
            .map(unitId -> {
                OrgNode unit = requireNode(tree, unitId);
                return new BlockingHead(unit.id(), unit.code(), unit.name(), heads.get(unit.headUserId()));
            })
            .toList();
    }

    @Override
    @Transactional
    public void move(UUID id, UUID parentId) {
        UUID rootId = orgRootLocator.rootId();
        // Khóa ghi gốc theo khóa chính - câu đầu tiên chạm CSDL; tuần tự hóa mọi lần đổi cha
        orgUnitRepository.findByIdForUpdate(rootId)
            .orElseThrow(() -> new IllegalStateException("Root org unit " + rootId + " is missing"));
        OrgUnit unit = orgUnitRepository.findByIdForUpdate(id).orElseThrow(OrgUnitGuards::notFound);
        if (Objects.equals(unit.getParentId(), parentId)) {
            return;
        }
        orgUnitRepository.findByIdForShare(parentId).orElseThrow(OrgUnitGuards::notFound);
        OrgTree tree = orgTreeService.load();
        validateMove(tree, requireNode(tree, id), requireNode(tree, parentId));

        // Khóa đọc tổ tiên cũ theo id tăng dần, đọc trưởng từ dòng vừa khóa
        Map<UUID, UUID> headByUnit = new LinkedHashMap<>();
        for (UUID ancestorId : OrgUnitRules.detachedAncestorIds(tree, id, parentId)) {
            OrgUnit ancestor = orgUnitRepository.findByIdForShare(ancestorId).orElseThrow(OrgUnitGuards::notFound);
            if (ancestor.getHeadUserId() != null) {
                headByUnit.put(ancestorId, ancestor.getHeadUserId());
            }
        }
        Map<UUID, OrgUserRef> seats = memberDirectory.lockForShare(headByUnit.values());
        // Trưởng ngồi ở đơn vị tạo sau ảnh chụp cây -> không xác định được vị trí, buộc tải lại
        if (seats.values().stream().anyMatch(seat -> !tree.contains(seat.orgUnitId()))) {
            throw new BusinessException(ErrorCode.CONFLICT, "Head seat changed while moving org unit " + id);
        }
        if (!OrgUnitRules.unitsWithDetachedHead(headByUnit, seatsByUser(seats), tree.subtreeIds(id)).isEmpty()) {
            throw new BusinessException(ErrorCode.ORG_UNIT_MOVE_DETACHES_HEAD);
        }

        OrgAuditChanges changes = new OrgAuditChanges().track(OrgAuditField.PARENT_ID, unit.getParentId(), parentId);
        unit.moveUnder(parentId);
        unit.markUpdated(currentActor.get().userId(), LocalDateTime.now(clock));
        orgAuditService.log(AuditAction.UPDATE, AuditTargetType.ORG_UNIT, unit.getId(), unit.getCode(), changes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitBlocker> checkDeletion(UUID id) {
        OrgTree tree = orgTreeService.load();
        ensure(OrgUnitRules.check(OrgUnitAction.DELETE, requireNode(tree, id), tree, true));
        return blockerService.deletionBlockers(tree, id);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        OrgUnit unit = orgUnitRepository.findByIdForUpdate(id).orElseThrow(OrgUnitGuards::notFound);
        OrgTree tree = orgTreeService.load();
        ensure(OrgUnitRules.check(OrgUnitAction.DELETE, requireNode(tree, id), tree, true));
        if (!blockerService.deletionBlockers(tree, id).isEmpty()) {
            throw new BusinessException(ErrorCode.ORG_UNIT_IN_USE);
        }

        // Audit trước lệnh xóa, cùng giao dịch: Bản chụp toàn dòng
        OrgAuditChanges snapshot = new OrgAuditChanges()
            .putOld(OrgAuditField.CODE, unit.getCode())
            .putOld(OrgAuditField.NAME, unit.getName())
            .putOld(OrgAuditField.UNIT_TYPE, unit.getUnitType())
            .putOld(OrgAuditField.PARENT_ID, unit.getParentId())
            .putOld(OrgAuditField.HEAD_USER_ID, unit.getHeadUserId())
            .putOld(OrgAuditField.STATUS, unit.getStatus());
        orgAuditService.log(AuditAction.DELETE, AuditTargetType.ORG_UNIT, unit.getId(), unit.getCode(), snapshot);
        try {
            orgUnitRepository.delete(unit);
            orgUnitRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            // RESTRICT là chốt chặn thật: Bảng chưa có nguồn vướng
            throw new BusinessException(ErrorCode.ORG_UNIT_IN_USE, "Org unit " + id + " is still referenced");
        }
    }

    // Sửa tên / trưởng; trả về cây đã nạp sau khóa để dựng phản hồi
    private OrgTree applyChanges(OrgUnit unit, String name, UUID headUserId) {
        boolean headChanged = !Objects.equals(unit.getHeadUserId(), headUserId);
        OrgUserRef head = null;
        if (headChanged && headUserId != null) {
            // Khóa đọc người được chọn: Va với vô hiệu hóa / chuyển đơn vị của chính người đó
            head = memberDirectory.lockForShare(Set.of(headUserId)).get(headUserId);
            if (head == null || !head.active()) {
                throw new BusinessException(ErrorCode.ORG_UNIT_HEAD_NOT_ACTIVE);
            }
        }
        // Nạp cây SAU khi khóa người được chọn
        OrgTree tree = orgTreeService.load();
        ensure(OrgUnitRules.check(OrgUnitAction.EDIT, requireNode(tree, unit.getId()), tree, true));
        if (head != null && !tree.isInSubtree(head.orgUnitId(), unit.getId())) {
            throw new BusinessException(ErrorCode.ORG_UNIT_HEAD_OUTSIDE_SUBTREE);
        }

        OrgAuditChanges changes = new OrgAuditChanges()
            .track(OrgAuditField.NAME, unit.getName(), name)
            .track(OrgAuditField.HEAD_USER_ID, unit.getHeadUserId(), headUserId);
        if (changes.isEmpty()) {
            return tree;
        }
        unit.rename(name);
        unit.assignHead(headUserId);
        unit.markUpdated(currentActor.get().userId(), LocalDateTime.now(clock));
        orgAuditService.log(AuditAction.UPDATE, AuditTargetType.ORG_UNIT, unit.getId(), unit.getCode(), changes);
        return tree;
    }

    // Cùng hàm kiểm cho move-check và move
    private static void validateMove(OrgTree tree, OrgNode node, OrgNode newParent) {
        ensure(OrgUnitRules.check(OrgUnitAction.MOVE, node, tree, true));
        ensure(OrgUnitRules.check(OrgUnitAction.ADD_CHILD, newParent, tree, true));
        ensure(OrgUnitRules.checkParentType(node.unitType(), newParent.unitType()));
        ensure(OrgUnitRules.checkNoCycle(tree, node.id(), newParent.id()));
    }

    private static OrgUnitView toView(OrgTree tree, OrgNode node, int depth, Map<UUID, OrgUnitHead> heads,
                                      boolean canManage) {
        OrgUnitHead head = node.headUserId() == null ? null : heads.get(node.headUserId());
        return new OrgUnitView(node.id(), node.code(), node.name(), node.unitType(), node.status(), node.parentId(),
            depth, head, OrgUnitRules.childTypeOf(node.unitType()),
            OrgUnitRules.allowedActions(node, tree, canManage));
    }

    private static Map<UUID, OrgUnitHead> headsOf(OrgTree tree, Collection<OrgUserRef> users) {
        return users.stream().collect(Collectors.toMap(OrgUserRef::id, user -> toHead(tree, user)));
    }

    private static OrgUnitHead toHead(OrgTree tree, OrgUserRef user) {
        String orgUnitName = tree.find(user.orgUnitId()).map(OrgNode::name).orElse(null);
        return new OrgUnitHead(user.id(), user.username(), user.fullName(), orgUnitName);
    }

    private static Map<UUID, UUID> seatsByUser(Map<UUID, OrgUserRef> users) {
        return users.values().stream().collect(Collectors.toMap(OrgUserRef::id, OrgUserRef::orgUnitId));
    }
}
