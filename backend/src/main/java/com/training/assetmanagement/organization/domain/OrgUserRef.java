package com.training.assetmanagement.organization.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// Thông tin người dùng mà miền tổ chức cần - do cổng OrgUnitMemberDirectory cung cấp
// avatarUrl: URL ký bản thu nhỏ, null khi chưa có ảnh
@Schema(name = "OrgUserRef", description = "User reference in organization domain")
public record OrgUserRef(UUID id, String username, String fullName, UUID orgUnitId, boolean active,
                         String avatarUrl) {
}
