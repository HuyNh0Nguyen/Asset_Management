package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.access.domain.RoleCode;
import com.training.assetmanagement.access.domain.RoleRef;
import com.training.assetmanagement.access.service.AdminRoleGuard;
import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.access.service.RoleAssignmentService;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;
import com.training.assetmanagement.notification.domain.*;
import com.training.assetmanagement.notification.service.NotificationPublisher;
import com.training.assetmanagement.organization.domain.OrgNode;
import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.organization.domain.OrgUnit;
import com.training.assetmanagement.organization.domain.OrgUnitStatus;
import com.training.assetmanagement.organization.service.OrganizationLookupService;
import com.training.assetmanagement.shared.config.AppProperties;
import com.training.assetmanagement.shared.persistence.ConstraintViolations;
import com.training.assetmanagement.shared.persistence.UuidV7;
import com.training.assetmanagement.shared.security.TemporaryPasswordGenerator;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.shared.web.FrontendRoute;
import com.training.assetmanagement.user.domain.*;
import com.training.assetmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationLookupService organizationLookupService;
    private final RoleAssignmentService roleAssignmentService;
    private final AdminRoleGuard adminRoleGuard;
    private final UserAuditService userAuditService;
    private final UserDetailAssembler assembler;
    private final NotificationPublisher notificationPublisher;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final CurrentActor currentActor;
    private final Clock clock;

    // Một giao dịch
    @Override
    @Transactional
    public UserDetail create(NewUser command) {
        // Khóa đọc đơn vị đích - câu đầu tiên
        organizationLookupService.requireActiveUnit(command.orgUnitId(), UserRules.ALLOWED_UNIT_TYPES);
        organizationLookupService.requireRank(command.rankId());
        List<RoleRef> roles = roleAssignmentService.requireRoles(command.roleIds());
        String email = command.email().trim();
        if (userRepository.existsByUsername(command.username())) {
            throw new BusinessException(ErrorCode.USERNAME_DUPLICATE);
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATE);
        }
        // Mật khẩu mặc định chỉ đi một đường: Biến cục bộ -> templateVars -> email
        String temporaryPassword = TemporaryPasswordGenerator.generate();
        ActorContext actor = currentActor.get();
        LocalDateTime now = LocalDateTime.now(clock);
        User user = User.create(UuidV7.generate(clock), command.username(), email, command.fullName().trim(),
            blankToNull(command.phoneNumber()), passwordEncoder.encode(temporaryPassword), command.orgUnitId(),
            command.rankId(), now.truncatedTo(ChronoUnit.SECONDS), actor.userId(), now);
        saveNew(user);
        roleAssignmentService.assign(user.getId(), command.roleIds());
        // Không mật khẩu, không hash
        userAuditService.log(AuditAction.CREATE, user.getId(), user.getUsername(), new AuditChanges()
            .putNew(UserAuditField.USERNAME, user.getUsername())
            .putNew(UserAuditField.EMAIL, user.getEmail())
            .putNew(UserAuditField.FULL_NAME, user.getFullName())
            .putNew(UserAuditField.PHONE_NUMBER, user.getPhoneNumber())
            .putNew(UserAuditField.ORG_UNIT_ID, user.getOrgUnitId())
            .putNew(UserAuditField.RANK_ID, user.getRankId())
            .putNew(UserAuditField.ROLES, RoleRef.codesOf(roles))
            .putNew(UserAuditField.STATUS, user.getStatus())
            .putNew(UserAuditField.MUST_CHANGE_PASSWORD, user.isMustChangePassword()));
        // Rollback -> không email
        sendAccountCreated(user, temporaryPassword);
        return assembler.assemble(user, organizationLookupService.loadTree());
    }

    // Thứ tự khóa: roles -> org_units -> users
    @Override
    @Transactional
    public UserDetail update(UUID id, UserProfileUpdate command) {
        // Khóa dòng ADMIN - câu đầu tiên, không xét trước đích có phải Admin
        adminRoleGuard.lockAdminRole();
        // Khóa đọc đơn vị đích - bảo đảm đơn vị hiện tại luôn ACTIVE
        organizationLookupService.requireActiveUnit(command.orgUnitId(), UserRules.ALLOWED_UNIT_TYPES);
        // Khóa ghi các đơn vị người này đang làm trưởng
        List<UUID> headedUnitIds = organizationLookupService.lockUnitsHeadedBy(id).stream().map(OrgUnit::getId).toList();
        User user = userRepository.findByIdForUpdate(id).orElseThrow(UserServiceImpl::notFound);
        UserRules.require(UserAction.EDIT, user.getStatus());
        organizationLookupService.requireRank(command.rankId());
        List<RoleRef> newRoles = roleAssignmentService.requireRoles(command.roleIds());
        List<RoleRef> oldRoles = roleAssignmentService.rolesOf(id);
        // Gỡ ADMIN của Admin ACTIVE cuối cùng -> chặn (kể cả tự gỡ)
        if (RoleRef.containsCode(oldRoles, RoleCode.ADMIN) && !RoleRef.containsCode(newRoles, RoleCode.ADMIN)) {
            adminRoleGuard.checkStillHasActiveAdmin(id);
        }
        // Nạp cây sau mọi khóa; tập khai phải bằng đúng tập cần thay
        OrgTree tree = organizationLookupService.loadTree();
        Map<UUID, UUID> replacements = HeadReplacements.match(
            HeadReplacements.mustReplace(headedUnitIds, command.orgUnitId(), tree), command.headReplacements()
        );

        String fullName = command.fullName().trim();
        String phoneNumber = blankToNull(command.phoneNumber());
        AuditChanges changes = new AuditChanges()
            .track(UserAuditField.FULL_NAME, user.getFullName(), fullName)
            .track(UserAuditField.PHONE_NUMBER, user.getPhoneNumber(), phoneNumber)
            .track(UserAuditField.ORG_UNIT_ID, user.getOrgUnitId(), command.orgUnitId())
            .track(UserAuditField.RANK_ID, user.getRankId(), command.rankId())
            .track(UserAuditField.ROLES, RoleRef.codesOf(oldRoles), RoleRef.codesOf(newRoles));
        // Không gì đổi -> không ghi, không Audit
        if (changes.isEmpty()) {
            return assembler.assemble(user, tree);
        }
        // flush: Đơn vị mới phải nằm trong CSDL trước replaceHead - chọn chính người bị chuyển
        // làm người thay sẽ vấp ORG_UNIT_HEAD_OUTSIDE_SUBTREE tự nhiên
        user.updateProfile(fullName, phoneNumber, command.orgUnitId(), command.rankId());
        user.markUpdated(currentActor.get().userId(), LocalDateTime.now(clock));
        userRepository.flush();
        // Không thu hồi token
        roleAssignmentService.replaceRoles(id, command.roleIds());
        // Audit đơn vị với actor = Admin đang thao tác
        replacements.forEach(organizationLookupService::replaceHead);
        userAuditService.log(AuditAction.UPDATE, user.getId(), user.getUsername(), changes);
        return assembler.assemble(user, tree);
    }

    // Giao dịch chỉ đọc, không khóa - lệnh thật luôn kiểm lại
    @Override
    @Transactional(readOnly = true)
    public List<UnitRef> checkUnitChange(UUID id, UUID orgUnitId) {
        User user = userRepository.findById(id).orElseThrow(UserServiceImpl::notFound);
        UserRules.require(UserAction.EDIT, user.getStatus());
        OrgTree tree = organizationLookupService.loadTree();
        OrgNode target = tree.find(orgUnitId).orElseThrow(UserServiceImpl::notFound);
        if (target.status() != OrgUnitStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ORG_UNIT_INACTIVE);
        }
        if (!UserRules.ALLOWED_UNIT_TYPES.contains(target.unitType())) {
            throw new BusinessException(ErrorCode.ORG_UNIT_TYPE_NOT_ALLOWED);
        }
        return HeadReplacements.mustReplace(organizationLookupService.unitsHeadedBy(id), orgUnitId, tree).stream()
            .map(unitId -> UserDetailAssembler.unitRef(tree, unitId))
            .toList();
    }

    // Hai Admin tạo cùng lúc -> bắt theo tên ràng buộc
    private void saveNew(User user) {
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            if (ConstraintViolations.violates(ex, UserConstraint.UK_USERS_USERNAME.getConstraintName())) {
                throw new BusinessException(ErrorCode.USERNAME_DUPLICATE);
            }
            if (ConstraintViolations.violates(ex, UserConstraint.UK_USERS_EMAIL.getConstraintName())) {
                throw new BusinessException(ErrorCode.EMAIL_DUPLICATE);
            }
            if (ConstraintViolations.violates(ex, UserConstraint.FK_USERS_RANK.getConstraintName())) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
            }
            throw ex;
        }
    }

    private void sendAccountCreated(User user, String temporaryPassword) {
        notificationPublisher.publishEmailOnly(new EmailNotification(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            EmailEventType.ACCOUNT_CREATED,
            EmailSubject.ACCOUNT_CREATED.getText(),
            appProperties.publicBaseUrl() + FrontendRoute.LOGIN.getPath(),
            Map.of(
                EmailTemplateVar.USERNAME.getKey(), user.getUsername(),
                EmailTemplateVar.TEMPORARY_PASSWORD.getKey(), temporaryPassword,
                EmailTemplateVar.LINK_LABEL.getKey(), EmailLinkLabel.LOGIN.getText()
            )
        ));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static BusinessException notFound() {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
