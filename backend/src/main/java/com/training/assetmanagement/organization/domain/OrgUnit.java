package com.training.assetmanagement.organization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// Bản chỉ đọc cho /api/me; hàng việc "Cây đơn vị" bổ sung đủ cột
@Entity
@Table(name = "org_units")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrgUnit {

    @Id
    private UUID id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;
}
