package com.training.assetmanagement.user.web.mapper;

import com.training.assetmanagement.user.domain.MeProfile;
import com.training.assetmanagement.user.web.dto.response.MeResponse;
import com.training.assetmanagement.user.web.dto.response.PermissionGrantResponse;

public final class MeResponseMapper {

    // grants là EnumMap -> quyền đi theo thứ tự enum
    public static MeResponse toResponse(MeProfile profile) {
        return new MeResponse(
            profile.id(),
            profile.username(),
            profile.fullName(),
            profile.email(),
            profile.phoneNumber(),
            profile.avatarUrl(),
            profile.mustChangePassword(),
            UserResponseMapper.toUnit(profile.orgUnit()),
            profile.rankName(),
            profile.roles().stream().map(UserResponseMapper::toRole).toList(),
            profile.grants().entrySet().stream()
                .map(entry -> new PermissionGrantResponse(entry.getKey(), entry.getValue()))
                .toList()
        );
    }

    private MeResponseMapper() {}
}
