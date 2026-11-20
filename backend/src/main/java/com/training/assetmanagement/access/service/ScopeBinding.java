package com.training.assetmanagement.access.service;

import jakarta.persistence.criteria.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// Mỗi tài nguyên khai cách tới thuộc tính đơn vị của nó - hợp đồng các giai đoạn sau hiện thực
public interface ScopeBinding<T> {

    // Đơn vị cho tác động và một vế của xem
    Expression<UUID> managingUnit(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);

    // TODO: Vế "hoặc đơn vị sử dụng" của xem (tài sản: đơn vị người giữ bản ghi OPEN - phase 3)
    default Optional<Predicate> usingUnitIn(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                            Set<UUID> unitIds) {
        return Optional.empty();
    }

    // Quan hệ SELF
    default Optional<Predicate> self(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, UUID userId) {
        return Optional.empty();
    }

    // Tài sản / lô / cấp phát = false; phiếu / người dùng = true
    boolean selfAppliesToAct();
}
