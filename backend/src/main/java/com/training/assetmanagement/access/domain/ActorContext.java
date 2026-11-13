package com.training.assetmanagement.access.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Principal của mỗi request - nạp lại mỗi request, không cache
@Schema(name = "ActorContext", description = "Contextual information about the actor performing an action")
public record ActorContext(
    UUID userId,
    String username,
    UUID orgUnitId,
    List<String> roleCodes,
    Map<Permission, DataScope> grants,
    boolean mustChangePassword
) implements Principal {

    @Override
    public String getName() {
        return username;
    }

    public boolean hasPermission(Permission permission) {
        return grants.containsKey(permission);
    }

    public Optional<DataScope> scopeOf(Permission permission) {
        return Optional.ofNullable(grants.get(permission));
    }
}
