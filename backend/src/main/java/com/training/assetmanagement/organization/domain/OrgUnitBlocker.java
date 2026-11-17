package com.training.assetmanagement.organization.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// Một nhóm vướng: Total đếm đúng, samples tối đa SAMPLE_LIMIT mục (FE hiện "và N mục khác")
@Schema(name = "OrgUnitBlocker", description = "A group of blockers for an organizational unit, including a total count and a sample list of items")
public record OrgUnitBlocker(OrgUnitBlockerKind kind, long total, List<OrgUnitBlockerItem> samples) {

    public static final int SAMPLE_LIMIT = 20;
}
