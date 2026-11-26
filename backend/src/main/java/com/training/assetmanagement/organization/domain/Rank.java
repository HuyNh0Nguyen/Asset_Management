package com.training.assetmanagement.organization.domain;

import com.training.assetmanagement.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// Cấp bậc - mã bất biến; tên và số thứ bậc sửa được kể cả khi đang dùng
@Entity
@Table(name = "ranks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rank extends BaseEntity {

    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    // Số lớn = bậc cao
    @Column(name = "seniority", nullable = false)
    private int seniority;

    private Rank(UUID id, String code, String name, int seniority) {
        super(id);
        this.code = code;
        this.name = name;
        this.seniority = seniority;
    }

    public static Rank create(UUID id, String code, String name, int seniority, UUID actorId, LocalDateTime now) {
        Rank rank = new Rank(id, code, name, seniority);
        rank.markCreated(actorId, now);
        return rank;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeSeniority(int seniority) {
        this.seniority = seniority;
    }
}
