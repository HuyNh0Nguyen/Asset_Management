package com.training.assetmanagement.shared.security;

import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

// Cổng: shared không phụ thuộc miền nào nên filter chỉ biết interface này
public interface ActorAuthenticator {

    // Rỗng khi token bị thu hồi hoặc tài khoản không còn ACTIVE
    Optional<Authentication> authenticate(UUID userId, Instant issuedAt);
}
