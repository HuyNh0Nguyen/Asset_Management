package com.training.assetmanagement.user.service;

import com.training.assetmanagement.user.domain.UserDetail;
import com.training.assetmanagement.user.domain.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserQueryService {

    Page<UserDetail> list(UserFilter filter, Pageable pageable);

    UserDetail get(UUID id);
}
