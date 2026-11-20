package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Schema(name = "UserProfileUpdate", description = "Represents an update to a user's profile, which may include changes to personal information, contact details, or other relevant attributes")
public record UserProfileUpdate(String fullName, String phoneNumber, UUID orgUnitId, UUID rankId,
                                Set<UUID> roleIds, List<HeadReplacement> headReplacements) {
}
