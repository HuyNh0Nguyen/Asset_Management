package com.training.assetmanagement.user.domain;

import com.training.assetmanagement.access.domain.RoleRef;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// Một người dùng đã ghép đủ đơn vị, cấp bậc, vai trò, URL ảnh - cho cả danh sách lẫn chi tiết
@Schema(name = "UserDetail", description = "Represents detailed information about a user, including their associated units, ranks, roles, and profile picture URL. This record is used for both listing users and providing detailed user information")
public record UserDetail(User user, UnitRef orgUnit, RankRef rank, List<RoleRef> roles, String avatarUrl,
                         List<UserAction> allowedActions) {
}
