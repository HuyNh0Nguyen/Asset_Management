package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// Khuôn OrgUnitBlocker: total đếm đúng, samples tối đa SAMPLE_LIMIT
@Schema(name = "UserBlocker", description = "Represents a user blocker, which includes the kind of blocker, the total count of items, and a list of sample items that illustrate the blocking condition.")
public record UserBlocker(UserBlockerKind kind, long total, List<UserBlockerItem> samples) {

    public static final int SAMPLE_LIMIT = 20;

    public static UserBlocker lastActiveAdmin() {
        return new UserBlocker(UserBlockerKind.LAST_ACTIVE_ADMIN, 1, List.of());
    }
}
