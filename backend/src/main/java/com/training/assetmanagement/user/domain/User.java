package com.training.assetmanagement.user.domain;

import com.training.assetmanagement.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

// Không bao giờ xóa dòng. Bốn cột bảo mật, status, avatar_image_id chỉ ghi bằng @Modifying UPDATE
// trong UserRepository - không có setter
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, updatable = false)
    private String username;

    @Column(name = "email", nullable = false, updatable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "org_unit_id", nullable = false)
    private UUID orgUnitId;

    @Column(name = "rank_id", nullable = false)
    private UUID rankId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "avatar_image_id")
    private UUID avatarImageId;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "token_valid_from", nullable = false)
    private LocalDateTime tokenValidFrom;

    private User(UUID id, String username, String email, String fullName, String phoneNumber,
                 String passwordHash, UUID orgUnitId, UUID rankId, LocalDateTime tokenValidFrom) {
        super(id);
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.orgUnitId = orgUnitId;
        this.rankId = rankId;
        this.status = UserStatus.ACTIVE;
        this.mustChangePassword = true;
        this.failedLoginCount = 0;
        this.tokenValidFrom = tokenValidFrom;
    }

    // Tài khoản mới: ACTIVE, bắt buộc đổi mật khẩu
    public static User create(UUID id, String username, String email, String fullName, String phoneNumber,
                              String passwordHash, UUID orgUnitId, UUID rankId, LocalDateTime tokenValidFrom,
                              UUID actorId, LocalDateTime now) {
        User user = new User(id, username, email, fullName, phoneNumber, passwordHash, orgUnitId, rankId,
            tokenValidFrom);
        user.markCreated(actorId, now);
        return user;
    }

    // Không đổi username / email; bên gọi markUpdated
    public void updateProfile(String fullName, String phoneNumber, UUID orgUnitId, UUID rankId) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.orgUnitId = orgUnitId;
        this.rankId = rankId;
    }

    public UserCredential toCredential() {
        return new UserCredential(getId(), username, email, fullName, passwordHash, status,
            mustChangePassword, failedLoginCount, lockedUntil);
    }
}
