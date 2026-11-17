package com.training.assetmanagement.organization.domain;

import java.util.UUID;

// Một mục vướng: Người dùng (code = username), đơn vị con, tài sản, lô
public record OrgUnitBlockerItem(UUID id, String code, String name, boolean inactive) {
}
