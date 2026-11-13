package com.training.assetmanagement.user.service;

import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.domain.UserAuthSnapshot;
import com.training.assetmanagement.user.domain.UserCredential;
import com.training.assetmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCredentialServiceImpl implements UserCredentialService {

    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findIdByUsername(String username) {
        return userRepository.findIdByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAuthSnapshot> findAuthSnapshot(UUID userId) {
        return userRepository.findAuthSnapshotById(userId);
    }

    @Override
    @Transactional
    public Optional<UserCredential> lockById(UUID userId) {
        return userRepository.findByIdForUpdate(userId).map(User::toCredential);
    }

    @Override
    @Transactional
    public void recordFailedLogin(UUID userId, int failedLoginCount, LocalDateTime lockedUntil) {
        userRepository.updateLoginFailures(userId, failedLoginCount, lockedUntil);
    }

    @Override
    @Transactional
    public void clearLoginFailures(UUID userId) {
        userRepository.updateLoginFailures(userId, 0, null);
    }

    @Override
    @Transactional
    public void updatePassword(UUID userId, String passwordHash, boolean mustChangePassword, UUID updatedBy) {
        userRepository.updatePassword(userId, passwordHash, mustChangePassword, revocationPoint(),
            updatedBy, LocalDateTime.now(clock));
    }

    @Override
    @Transactional
    public void revokeTokens(UUID userId) {
        userRepository.updateTokenValidFrom(userId, revocationPoint());
    }

    // Cắt về giây - đăng nhập lại trong cùng giây vẫn hợp lệ
    private LocalDateTime revocationPoint() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }
}
