package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// detail: Số lượng đang giữ (OPEN_ALLOCATION) / tên bước duyệt (NAMED_APPROVER)
@Schema(name = "UserBlockerItem", description = "Details of a user blocker item, including its unique identifier, code, name, and additional details")
public record UserBlockerItem(UUID id, String code, String name, String detail) {
}
