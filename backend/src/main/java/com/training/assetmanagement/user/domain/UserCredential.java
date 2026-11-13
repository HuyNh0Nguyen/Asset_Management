package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

// Bản chụp bất biến cho miền auth - auth không cầm entity nên không ghi vòng qua dirty-checking
@Schema(name = "UserCredential", description = "User credential information for authentication")
public record UserCredential(
    UUID id,
    String username,
    String email,
    String fullName,
    String passwordHash,
    UserStatus status,
    boolean mustChangePassword,
    int failedLoginCount,
    LocalDateTime lockedUntil
) {
    // Không bao giờ in passwordHash ra log
    @Override
    public String toString() {
        return "UserCredential[id=" + id + ", username=" + username + ", status=" + status + "]";
    }
}
