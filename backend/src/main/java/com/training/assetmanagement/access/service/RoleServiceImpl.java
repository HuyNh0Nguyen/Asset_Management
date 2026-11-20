package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.*;
import com.training.assetmanagement.access.repository.RoleHolderCount;
import com.training.assetmanagement.access.repository.RolePermissionRepository;
import com.training.assetmanagement.access.repository.RoleRepository;
import com.training.assetmanagement.access.repository.UserRoleRepository;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;
import com.training.assetmanagement.shared.persistence.ConstraintViolations;
import com.training.assetmanagement.shared.persistence.UuidV7;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleAuditService roleAuditService;
    private final CurrentActor currentActor;
    private final Clock clock;

    // Trả trọn, sắp theo tên tiếng Việt - vài vai trò, form người dùng cần đủ
    @Override
    @Transactional(readOnly = true)
    public List<RoleDetail> list() {
        List<Role> roles = new ArrayList<>(roleRepository.findAll());
        roles.sort(Comparator.comparing(Role::getName, Collator.getInstance(VIETNAMESE)));
        Map<UUID, List<RoleGrant>> grants = grantsByRole(roles.stream().map(Role::getId).toList());
        Map<UUID, Long> holders = userRoleRepository.countHoldersByRole().stream()
            .collect(Collectors.toMap(RoleHolderCount::roleId, RoleHolderCount::total));
        return roles.stream()
            .map(role -> toDetail(role, grants.getOrDefault(role.getId(), List.of()),
                holders.getOrDefault(role.getId(), 0L)))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetail get(UUID id) {
        Role role = roleRepository.findById(id).orElseThrow(RoleServiceImpl::notFound);
        return toDetail(role, grantsOf(id), userRoleRepository.countByRoleId(id));
    }

    @Override
    @Transactional
    public RoleDetail create(String code, String name, String description, List<RoleGrant> grants) {
        RoleRules.checkGrants(grants);
        if (roleRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.ROLE_CODE_DUPLICATE);
        }
        UUID actorId = currentActor.get().userId();
        LocalDateTime now = LocalDateTime.now(clock);
        Role role = Role.create(UuidV7.generate(clock), code, name.trim(), normalize(description), actorId, now);
        try {
            roleRepository.saveAndFlush(role);
        } catch (DataIntegrityViolationException ex) {
            // Hai Admin tạo cùng mã cùng lúc
            if (ConstraintViolations.violates(ex, RoleConstraint.UK_ROLES_CODE.getConstraintName())) {
                throw new BusinessException(ErrorCode.ROLE_CODE_DUPLICATE);
            }
            throw ex;
        }
        List<RoleGrant> sorted = RoleGrant.sorted(grants);
        rolePermissionRepository.saveAll(sorted.stream()
            .map(grant -> RolePermission.create(UuidV7.generate(clock), role.getId(), grant, actorId, now))
            .toList());
        roleAuditService.log(AuditAction.CREATE, role.getId(), role.getCode(), new AuditChanges()
            .putNew(RoleAuditField.CODE, role.getCode())
            .putNew(RoleAuditField.NAME, role.getName())
            .putNew(RoleAuditField.DESCRIPTION, role.getDescription())
            .putNew(RoleAuditField.PERMISSIONS, sorted));
        return toDetail(role, sorted, 0);
    }

    @Override
    @Transactional
    public RoleDetail update(UUID id, String name, String description, List<RoleGrant> grants) {
        // Khóa ghi dòng vai trò - câu đầu tiên
        Role role = roleRepository.findByIdForUpdate(id).orElseThrow(RoleServiceImpl::notFound);
        RoleRules.checkEditable(role);
        RoleRules.checkGrants(grants);

        List<RolePermission> cells = rolePermissionRepository.findByRoleId(id);
        List<RoleGrant> before = RoleGrant.sorted(cells.stream().map(RolePermission::toGrant).toList());
        List<RoleGrant> after = RoleGrant.sorted(grants);
        String newName = name.trim();
        String newDescription = normalize(description);
        AuditChanges changes = new AuditChanges()
            .track(RoleAuditField.NAME, role.getName(), newName)
            .track(RoleAuditField.DESCRIPTION, role.getDescription(), newDescription)
            .track(RoleAuditField.PERMISSIONS, before, after);
        long holderCount = userRoleRepository.countByRoleId(id);
        // Không gì đổi -> không ghi, không Audit
        if (changes.isEmpty()) {
            return toDetail(role, before, holderCount);
        }

        UUID actorId = currentActor.get().userId();
        LocalDateTime now = LocalDateTime.now(clock);
        if (!Objects.equals(role.getName(), newName) || !Objects.equals(role.getDescription(), newDescription)) {
            role.rename(newName, newDescription, actorId, now);
        }
        applyMatrix(role.getId(), cells, after, actorId, now);
        roleAuditService.log(AuditAction.UPDATE, role.getId(), role.getCode(), changes);
        // Không thu hồi token: Người giữ nhận quyền mới ở request kế tiếp
        return toDetail(role, after, holderCount);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Role role = roleRepository.findByIdForUpdate(id).orElseThrow(RoleServiceImpl::notFound);
        RoleRules.checkEditable(role);
        // Bản chụp trước lệnh xóa, cùng giao dịch
        roleAuditService.log(AuditAction.DELETE, role.getId(), role.getCode(), new AuditChanges()
            .putOld(RoleAuditField.CODE, role.getCode())
            .putOld(RoleAuditField.NAME, role.getName())
            .putOld(RoleAuditField.DESCRIPTION, role.getDescription())
            .putOld(RoleAuditField.PERMISSIONS, grantsOf(id)));
        try {
            roleRepository.delete(role);
            roleRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            // RESTRICT từ user_roles / workflow_approver_rules chính là quy tắc - không đếm trước
            if (ConstraintViolations.violates(ex, RoleConstraint.FK_USER_ROLES_ROLE.getConstraintName())
                || ConstraintViolations.violates(ex, RoleConstraint.FK_WORKFLOW_APPROVER_RULES_ROLE.getConstraintName())) {
                throw new BusinessException(ErrorCode.ROLE_IN_USE);
            }
            throw ex;
        }
    }

    // Xóa ô bị bỏ, sửa phạm vi ô đổi, chèn ô mới
    private void applyMatrix(UUID roleId, List<RolePermission> cells, List<RoleGrant> wanted,
                             UUID actorId, LocalDateTime now) {
        Map<Permission, DataScope> pending = new EnumMap<>(Permission.class);
        wanted.forEach(grant -> pending.put(grant.permission(), grant.scope()));
        List<RolePermission> removed = new ArrayList<>();
        for (RolePermission cell : cells) {
            DataScope scope = pending.remove(cell.getPermission());
            if (scope == null) {
                removed.add(cell);
            } else if (scope != cell.getScope()) {
                cell.changeScope(scope, actorId, now);
            }
        }
        rolePermissionRepository.deleteAll(removed);
        pending.forEach((permission, scope) -> rolePermissionRepository.save(RolePermission.create(
            UuidV7.generate(clock), roleId, new RoleGrant(permission, scope), actorId, now
        )));
    }

    private List<RoleGrant> grantsOf(UUID roleId) {
        return RoleGrant.sorted(rolePermissionRepository.findByRoleId(roleId).stream()
            .map(RolePermission::toGrant).toList());
    }

    private Map<UUID, List<RoleGrant>> grantsByRole(Collection<UUID> roleIds) {
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, List<RoleGrant>> result = new HashMap<>();
        for (RolePermission cell : rolePermissionRepository.findByRoleIdIn(roleIds)) {
            result.computeIfAbsent(cell.getRoleId(), key -> new ArrayList<>()).add(cell.toGrant());
        }
        result.replaceAll((roleId, grants) -> RoleGrant.sorted(grants));
        return result;
    }

    private static RoleDetail toDetail(Role role, List<RoleGrant> grants, long holderCount) {
        return new RoleDetail(role, grants, holderCount, RoleRules.allowedActions(role, holderCount));
    }

    private static String normalize(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }

    private static BusinessException notFound() {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
