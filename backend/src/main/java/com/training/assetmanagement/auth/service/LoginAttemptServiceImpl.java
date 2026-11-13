package com.training.assetmanagement.auth.service;

import com.training.assetmanagement.access.service.ActorContextService;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditActor;
import com.training.assetmanagement.audit.domain.AuditEvent;
import com.training.assetmanagement.audit.domain.AuditTargetType;
import com.training.assetmanagement.audit.service.AuditLogger;
import com.training.assetmanagement.auth.domain.AuthAuditField;
import com.training.assetmanagement.auth.domain.LoginFailureReason;
import com.training.assetmanagement.auth.domain.LoginLockPolicy;
import com.training.assetmanagement.auth.domain.LoginOutcome;
import com.training.assetmanagement.user.domain.UserCredential;
import com.training.assetmanagement.user.domain.UserStatus;
import com.training.assetmanagement.user.service.UserCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptServiceImpl.class);

    private final UserCredentialService userCredentialService;
    private final ActorContextService actorContextService;
    private final AuditLogger auditLogger;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    // Hash giả: Username không tồn tại vẫn tồn đúng một lần BCrypt
    private final String dummyHash;

    public LoginAttemptServiceImpl(UserCredentialService userCredentialService,
                                   ActorContextService actorContextService,
                                   AuditLogger auditLogger,
                                   PasswordEncoder passwordEncoder,
                                   Clock clock) {
        this.userCredentialService = userCredentialService;
        this.actorContextService = actorContextService;
        this.auditLogger = auditLogger;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public LoginOutcome attempt(String username, String rawPassword) {
        // Tra username không khóa, rồi mới khóa dòng theo id
        Optional<UserCredential> found = userCredentialService.findIdByUsername(username)
            .flatMap(userCredentialService::lockById);
        if (found.isEmpty()) {
            // Thời gian phản hồi không lộ username có tồn tại; không ghi Audit
            passwordEncoder.matches(rawPassword, dummyHash);
            log.warn("Login failed for unknown username '{}'", username);
            return new LoginOutcome.InvalidCredentials();
        }
        return evaluate(found.get(), rawPassword, LocalDateTime.now(clock));
    }

    private LoginOutcome evaluate(UserCredential user, String rawPassword, LocalDateTime now) {
        // actor_id = chính tài khoản đó; actor_roles từ truy vấn
        AuditActor actor = new AuditActor(user.id(), actorContextService.loadGrants(user.id()).roleCodes());

        // Đang khóa: Không kiểm mật khẩu, kể cả đúng
        if (LoginLockPolicy.isLocked(user.lockedUntil(), now)) {
            auditFailure(actor, user, reasonOnly(LoginFailureReason.LOCKED));
            return new LoginOutcome.Locked();
        }

        int failedCount = user.failedLoginCount();
        if (LoginLockPolicy.isLockExpired(user.lockedUntil(), now)) {
            // Hết khóa thì bộ đếm về 0 TRƯỚC khi xét tiếp
            userCredentialService.clearLoginFailures(user.id());
            failedCount = 0;
        }

        if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
            return recordBadPassword(actor, user, failedCount + 1, now);
        }

        // INACTIVE chỉ lộ khi mật khẩu đúng - người dò không biết tài khoản đã nghỉ
        if (user.status() != UserStatus.ACTIVE) {
            auditFailure(actor, user, reasonOnly(LoginFailureReason.INACTIVE));
            return new LoginOutcome.Disabled();
        }

        if (failedCount > 0) {
            userCredentialService.clearLoginFailures(user.id());
        }
        auditLogger.log(actor, AuditEvent.withoutChange(
            AuditAction.LOGIN_SUCCESS, AuditTargetType.USER, user.id(), user.username()
        ));
        return new LoginOutcome.Success(user.id(), user.mustChangePassword());
    }

    private LoginOutcome recordBadPassword(AuditActor actor, UserCredential user, int failedCount, LocalDateTime now) {
        LocalDateTime lockedUntil = LoginLockPolicy.lockedUntilAfterFailure(failedCount, now);
        userCredentialService.recordFailedLogin(user.id(), failedCount, lockedUntil);

        Map<String, Object> newValue = reasonOnly(LoginFailureReason.BAD_PASSWORD);
        newValue.put(AuthAuditField.FAILED_LOGIN_COUNT.getKey(), failedCount);
        if (lockedUntil != null) {
            newValue.put(AuthAuditField.LOCKED_UNTIL.getKey(), lockedUntil.toString());
        }
        auditFailure(actor, user, newValue);
        // Lần sai thứ 5 -> Báo khóa luôn
        return lockedUntil != null ? new LoginOutcome.Locked() : new LoginOutcome.InvalidCredentials();
    }

    private void auditFailure(AuditActor actor, UserCredential user, Map<String, Object> newValue) {
        auditLogger.log(actor, new AuditEvent(
            AuditAction.LOGIN_FAILURE, AuditTargetType.USER, user.id(), user.username(), null, newValue
        ));
    }

    private static Map<String, Object> reasonOnly(LoginFailureReason reason) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put(AuthAuditField.REASON.getKey(), reason.name());
        return value;
    }
}
