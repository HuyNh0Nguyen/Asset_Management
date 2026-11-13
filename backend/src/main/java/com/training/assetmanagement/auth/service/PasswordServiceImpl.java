package com.training.assetmanagement.auth.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditActor;
import com.training.assetmanagement.audit.domain.AuditEvent;
import com.training.assetmanagement.audit.domain.AuditTargetType;
import com.training.assetmanagement.audit.service.AuditLogger;
import com.training.assetmanagement.auth.domain.AuthAuditField;
import com.training.assetmanagement.auth.domain.TemporaryPasswordGenerator;
import com.training.assetmanagement.notification.domain.*;
import com.training.assetmanagement.notification.service.NotificationPublisher;
import com.training.assetmanagement.shared.config.AppProperties;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.shared.web.FrontendRoute;
import com.training.assetmanagement.user.domain.UserCredential;
import com.training.assetmanagement.user.domain.UserStatus;
import com.training.assetmanagement.user.service.UserCredentialService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordServiceImpl.class);

    private final CurrentActor currentActor;
    private final UserCredentialService userCredentialService;
    private final AuditLogger auditLogger;
    private final NotificationPublisher notificationPublisher;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void changePassword(String newPassword) {
        ActorContext actor = currentActor.get();
        UserCredential user = userCredentialService.lockById(actor.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        // Độ mạnh + xác nhận khớp đã do @Valid kiểm (400)
        if (passwordEncoder.matches(newPassword, user.passwordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_CURRENT);
        }
        userCredentialService.updatePassword(user.id(), passwordEncoder.encode(newPassword), false, actor.userId());

        // Chỉ ghi trường thay đổi - không bao giờ ghi mật khẩu
        Map<String, Object> oldValue = user.mustChangePassword() ? mustChangePassword(true) : null;
        Map<String, Object> newValue = user.mustChangePassword() ? mustChangePassword(false) : null;
        auditLogger.log(toAuditActor(actor), new AuditEvent(
            AuditAction.PASSWORD_CHANGE, AuditTargetType.USER, user.id(), user.username(), oldValue, newValue
        ));
        log.info("Password changed for user {}", user.id());
    }

    @Override
    @Transactional
    public void resetPassword(UUID userId) {
        ActorContext admin = currentActor.get();
        UserCredential target = userCredentialService.lockById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        if (target.status() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_NOT_ACTIVE);
        }

        // Mật khẩu tạm chỉ đi một đường: Biến cục bộ -> templateVars -> RabbitMQ -> Email
        String temporaryPassword = TemporaryPasswordGenerator.generate();
        // Một lệnh ghi: Hash, bật cờ, thu hồi token, gỡ khóa + reset bộ đếm
        userCredentialService.updatePassword(target.id(), passwordEncoder.encode(temporaryPassword), true, admin.userId());

        auditLogger.log(toAuditActor(admin), new AuditEvent(
            AuditAction.PASSWORD_RESET, AuditTargetType.USER, target.id(), target.username(),
            target.mustChangePassword() ? null : mustChangePassword(false), mustChangePassword(true)
        ));

        notificationPublisher.publishEmailOnly(new EmailNotification(
            target.id(),
            target.email(),
            target.fullName(),
            EmailEventType.PASSWORD_RESET,
            EmailSubject.PASSWORD_RESET.getText(),
            appProperties.publicBaseUrl() + FrontendRoute.LOGIN.getPath(),
            Map.of(
                EmailTemplateVar.USERNAME.getKey(), target.username(),
                EmailTemplateVar.TEMPORARY_PASSWORD.getKey(), temporaryPassword,
                EmailTemplateVar.LINK_LABEL.getKey(), EmailLinkLabel.LOGIN.getText()
            )
        ));
        log.info("Password reset for user {} by {}", target.id(), admin.userId());
    }

    @Override
    @Transactional
    public void logout() {
        ActorContext actor = currentActor.get();
        userCredentialService.revokeTokens(actor.userId());
        auditLogger.log(toAuditActor(actor), AuditEvent.withoutChange(
            AuditAction.LOGOUT, AuditTargetType.USER, actor.userId(), actor.username()
        ));
    }

    private static AuditActor toAuditActor(ActorContext actor) {
        return new AuditActor(actor.userId(), actor.roleCodes());
    }

    private static Map<String, Object> mustChangePassword(boolean value) {
        return Map.of(AuthAuditField.MUST_CHANGE_PASSWORD.getKey(), value);
    }
}
