package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// headedUnits không chặn - màn hình bắt chọn người thay hoặc để trống cho từng đơn vị
@Schema(name = "UserDeactivationCheck", description = "Represents a check for user deactivation, indicating whether the user can be deactivated and any relevant details about the deactivation process")
public record UserDeactivationCheck(List<UserBlocker> blockers, List<UnitRef> headedUnits) {
}
