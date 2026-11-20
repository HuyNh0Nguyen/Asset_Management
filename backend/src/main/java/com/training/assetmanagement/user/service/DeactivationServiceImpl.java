package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.access.service.AdminRoleGuard;
import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;
import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.organization.domain.OrgUnit;
import com.training.assetmanagement.organization.service.OrganizationLookupService;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.user.domain.*;
import com.training.assetmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Vô hiệu hóa + kích hoạt lại
@Service
@RequiredArgsConstructor
public class DeactivationServiceImpl implements DeactivationService {

    private final UserRepository userRepository;
    private final UserCredentialService userCredentialService;
    private final OrganizationLookupService organizationLookupService;
    private final AdminRoleGuard adminRoleGuard;
    private final List<UserDeactivationBlockerSource> blockerSources;
    private final ObjectProvider<UserDeactivationCascade> cascades;
    private final UserAuditService userAuditService;
    private final CurrentActor currentActor;
    private final Clock clock;

    // Chỉ để trình bày
    @Override
    @Transactional(readOnly = true)
    public UserDeactivationCheck check(UUID id) {
        User user = userRepository.findById(id).orElseThrow(DeactivationServiceImpl::notFound);
        UserRules.require(UserAction.DEACTIVATE, user.getStatus());
        List<UserBlocker> blockers = new ArrayList<>();
        if (adminRoleGuard.isLastActiveAdmin(id)) {
            blockers.add(UserBlocker.lastActiveAdmin());
        }
        blockerSources.forEach(source -> blockers.addAll(source.deactivationBlockers(id)));
        OrgTree tree = organizationLookupService.loadTree();
        List<UnitRef> headedUnits = organizationLookupService.unitsHeadedBy(id).stream()
            .map(unitId -> UserDetailAssembler.unitRef(tree, unitId))
            .toList();
        return new UserDeactivationCheck(blockers, headedUnits);
    }

    // Một giao dịch; người thực hiện mọi thay đổi dây chuyền = Admin đang thao tác
    @Override
    @Transactional
    public void deactivate(UUID id, List<HeadReplacement> headReplacements) {
        // Khóa dòng ADMIN
        adminRoleGuard.lockAdminRole();
        List<UUID> headedUnitIds = organizationLookupService.lockUnitsHeadedBy(id).stream().map(OrgUnit::getId).toList();
        User user = userRepository.findByIdForUpdate(id).orElseThrow(DeactivationServiceImpl::notFound);
        UserRules.require(UserAction.DEACTIVATE, user.getStatus());
        String username = user.getUsername();
        if (adminRoleGuard.holdsAdmin(id)) {
            adminRoleGuard.checkStillHasActiveAdmin(id);
        }
        // Kiểm lại dưới khóa - mọi đường tạo thứ chặn đều khóa đọc dòng users
        if (blockerSources.stream().anyMatch(source -> !source.deactivationBlockers(id).isEmpty())) {
            throw new BusinessException(ErrorCode.USER_DEACTIVATION_BLOCKED);
        }
        Map<UUID, UUID> replacements = HeadReplacements.match(headedUnitIds, headReplacements);
        // CAS + thu hồi token
        ActorContext actor = currentActor.get();
        if (userRepository.updateStatus(id, UserStatus.ACTIVE, UserStatus.INACTIVE, actor.userId(),
            LocalDateTime.now(clock)) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        userCredentialService.revokeTokens(id);
        // Chọn chính đich làm người thay sẽ ấp ORG_UNIT_HEAD_NOT_ACTIVE
        replacements.forEach(organizationLookupService::replaceHead);
        cascades.orderedStream().forEach(cascade -> cascade.onUserDeactivated(id));
        userAuditService.log(AuditAction.DEACTIVATE, id, username, new AuditChanges()
            .track(UserAuditField.STATUS, UserStatus.ACTIVE, UserStatus.INACTIVE));
    }

    // Không sinh mật khẩu mới, không email - chỉ bật cờ bắt buộc đổi mật khẩu
    @Override
    @Transactional
    public void reactivate(UUID id, UUID orgUnitId) {
        // Khóa đọc đơn vị đích - câu đầu tiên
        organizationLookupService.requireActiveUnit(orgUnitId, UserRules.ALLOWED_UNIT_TYPES);
        User user = userRepository.findByIdForUpdate(id).orElseThrow(DeactivationServiceImpl::notFound);
        UserRules.require(UserAction.REACTIVATE, user.getStatus());
        UUID oldUnitId = user.getOrgUnitId();
        boolean oldMustChange = user.isMustChangePassword();
        String username = user.getUsername();
        // CAS INACTIVE -> ACTIVE
        if (userRepository.reactivate(id, orgUnitId, true, UserStatus.INACTIVE, UserStatus.ACTIVE,
            currentActor.get().userId(), LocalDateTime.now(clock)) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        userAuditService.log(AuditAction.REACTIVATE, id, username, new AuditChanges()
            .track(UserAuditField.STATUS, UserStatus.INACTIVE, UserStatus.ACTIVE)
            .track(UserAuditField.ORG_UNIT_ID, oldUnitId, orgUnitId)
            .track(UserAuditField.MUST_CHANGE_PASSWORD, oldMustChange, true));
    }

    private static BusinessException notFound() {
        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
