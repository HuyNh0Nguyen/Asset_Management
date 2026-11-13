package com.training.assetmanagement.auth.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.access.service.ActorContextService;
import com.training.assetmanagement.shared.security.ActorAuthenticator;
import com.training.assetmanagement.shared.security.SecurityAuthority;
import com.training.assetmanagement.user.domain.UserAuthSnapshot;
import com.training.assetmanagement.user.domain.UserStatus;
import com.training.assetmanagement.user.service.UserCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Đặt ở auth: auth -> user, auth -> access là chiều duy nhất, không tạo vòng
@Service
@RequiredArgsConstructor
public class ActorAuthenticatorImpl implements ActorAuthenticator {

    private final UserCredentialService userCredentialService;
    private final ActorContextService actorContextService;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Optional<Authentication> authenticate(UUID userId, Instant issuedAt) {
        return userCredentialService.findAuthSnapshot(userId)
            .filter(snapshot -> isIssuedAfterRevocation(issuedAt, snapshot.tokenValidFrom()))
            .filter(snapshot -> snapshot.status() == UserStatus.ACTIVE)
            .map(this::toAuthentication);
    }

    // Hợp lệ khi iat >= token_valid_from (cột lưu giờ +07 -> đổi bằng zone của Clock)
    private boolean isIssuedAfterRevocation(Instant issuedAt, LocalDateTime tokenValidFrom) {
        Instant revokedAt = tokenValidFrom.atZone(clock.getZone()).toInstant();
        return !issuedAt.isBefore(revokedAt);
    }

    private Authentication toAuthentication(UserAuthSnapshot snapshot) {
        ActorContext actor = actorContextService.buildContext(
            snapshot.id(), snapshot.username(), snapshot.orgUnitId(), snapshot.mustChangePassword()
        );
        // authorities = mã quyền (cho @PreAuthorize) + cờ bắt buộc đổi mật khẩu
        List<GrantedAuthority> authorities = new ArrayList<>();
        actor.grants().keySet().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.name())));
        if (actor.mustChangePassword()) {
            authorities.add(new SimpleGrantedAuthority(SecurityAuthority.PASSWORD_CHANGE_REQUIRED.name()));
        }
        return UsernamePasswordAuthenticationToken.authenticated(actor, null, authorities);
    }
}
