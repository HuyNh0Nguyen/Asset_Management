package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.organization.domain.OrgUnitBlocker;

import java.util.List;
import java.util.UUID;

public interface OrgUnitBlockerService {

    // Người dùng ACTIVE, đơn vị con ACTIVE, tài sản / lô chưa thanh lý hết
    List<OrgUnitBlocker> deactivationBlockers(OrgTree tree, UUID unitId);

    // Mọi người dùng, mọi đơn vị con, mọi tài sản / lô
    List<OrgUnitBlocker> deletionBlockers(OrgTree tree, UUID unitId);
}
