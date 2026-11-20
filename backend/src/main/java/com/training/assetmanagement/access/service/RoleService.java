package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.RoleDetail;
import com.training.assetmanagement.access.domain.RoleGrant;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    List<RoleDetail> list();

    RoleDetail get(UUID id);

    RoleDetail create(String code, String name, String description, List<RoleGrant> grants);

    RoleDetail update(UUID id, String name, String description, List<RoleGrant> grants);

    void delete(UUID id);
}
