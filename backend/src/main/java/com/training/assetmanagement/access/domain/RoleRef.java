package com.training.assetmanagement.access.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// Vai trò rút gọn cho miền khác (user): id, mã, tên
public record RoleRef(UUID id, String code, String name) {

    // Audit ghi danh sách mã đã sắp
    public static List<String> codesOf(Collection<RoleRef> roles) {
        return roles.stream().map(RoleRef::code).sorted().toList();
    }

    public static boolean containsCode(Collection<RoleRef> roles, RoleCode code) {
        return roles.stream().anyMatch(role -> code.matches(role.code()));
    }
}
