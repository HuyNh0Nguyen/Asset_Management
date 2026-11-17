package com.training.assetmanagement.organization.domain;

import io.swagger.v3.oas.annotations.media.Schema;

// Một nút trong thứ tự tiền thứ tự (cha trước con), depth của gốc = 0
@Schema(name = "OrgTreeEntry", description = "A node in the organization tree, in pre-order (parent before children), with depth of root = 0")
public record OrgTreeEntry(OrgNode node, int depth) {
}
