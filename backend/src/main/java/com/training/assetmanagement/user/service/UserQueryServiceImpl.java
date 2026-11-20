package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.access.service.ActorScope;
import com.training.assetmanagement.access.service.RoleAssignmentService;
import com.training.assetmanagement.access.service.ScopeQueryBuilder;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.domain.UserAttribute;
import com.training.assetmanagement.user.domain.UserDetail;
import com.training.assetmanagement.user.domain.UserFilter;
import com.training.assetmanagement.user.repository.UserRepository;
import com.training.assetmanagement.user.repository.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

// USER_MANAGE chỉ có ALL nên kết quả luôn "tất cả" - vẫn đi qua ScopeQueryBuilder để kênh đầu tiên đúng khuôn
@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private static final Sort DEFAULT_SORT = Sort.by(UserAttribute.USERNAME.getPath());

    private final UserRepository userRepository;
    private final ScopeQueryBuilder scopeQueryBuilder;
    private final UserScopeBinding userScopeBinding;
    private final RoleAssignmentService roleAssignmentService;
    private final UserDetailAssembler assembler;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDetail> list(UserFilter filter, Pageable pageable) {
        ActorScope scope = scopeQueryBuilder.forCurrentActor();
        Set<UUID> roleUserIds = filter.roleId() == null ? null : roleAssignmentService.userIdsWithRole(filter.roleId());
        Specification<User> spec = scope.viewSpec(Permission.USER_MANAGE, userScopeBinding)
            .and(UserSpecifications.matching(filter, scope.getTree(), roleUserIds));
        Page<User> page = userRepository.findAll(spec, withSafeSort(pageable));
        return new PageImpl<>(assembler.assemble(page.getContent(), scope.getTree()), page.getPageable(),
            page.getTotalElements());
    }

    // Ngoài vị từ xem -> 404 (không lộ tồn tại)
    @Override
    @Transactional(readOnly = true)
    public UserDetail get(UUID id) {
        ActorScope scope = scopeQueryBuilder.forCurrentActor();
        Specification<User> spec = scope.viewSpec(Permission.USER_MANAGE, userScopeBinding)
            .and(UserSpecifications.hasId(id));
        User user = userRepository.findOne(spec)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        return assembler.assemble(user, scope.getTree());
    }

    // sort chỉ nhận username / fullName / createdAt - trường khác -> 400
    private static Pageable withSafeSort(Pageable pageable) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : DEFAULT_SORT;
        for (Sort.Order order : sort) {
            if (UserAttribute.sortableByPath(order.getProperty()).isEmpty()) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Unsupported sort " + order.getProperty());
            }
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
