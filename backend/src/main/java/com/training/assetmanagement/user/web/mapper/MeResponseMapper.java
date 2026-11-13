package com.training.assetmanagement.user.web.mapper;

import com.training.assetmanagement.user.domain.MeProfile;
import com.training.assetmanagement.user.web.dto.response.MeResponse;
import com.training.assetmanagement.user.web.dto.response.OrgUnitRefResponse;
import com.training.assetmanagement.user.web.dto.response.PermissionGrantResponse;

import java.util.List;

public final class MeResponseMapper {

    public static MeResponse toResponse(MeProfile profile) {
        // grants là EnumMap -> danh sách quyền theo thứ tự khai báo của enum
        List<PermissionGrantResponse> permissions = profile.grants().entrySet().stream()
            .map(entry -> new PermissionGrantResponse(entry.getKey(), entry.getValue()))
            .toList();
        return new MeResponse(
            profile.id(),
            profile.username(),
            profile.fullName(),
            profile.email(),
            null,
            profile.mustChangePassword(),
            new OrgUnitRefResponse(profile.orgUnitId(), profile.orgUnitCode(), profile.orgUnitName()),
            profile.rankName(),
            permissions
        );
    }

    private MeResponseMapper() {}
}
