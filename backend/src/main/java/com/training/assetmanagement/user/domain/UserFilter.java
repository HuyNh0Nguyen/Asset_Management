package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// q khớp username / họ tên / email; orgUnitId = cả cây con
@Schema(name = "UserFilter", description = "Represents a filter criteria for searching or filtering users based on various attributes such as username, full name, email, and organizational unit ID. The orgUnitId can include all child units in the hierarchy")
public record UserFilter(String q, UUID orgUnitId, UUID roleId, UserStatus status) {
}
