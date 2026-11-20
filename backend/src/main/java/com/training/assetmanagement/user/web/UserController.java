package com.training.assetmanagement.user.web;

import com.training.assetmanagement.shared.web.*;
import com.training.assetmanagement.user.domain.NewUser;
import com.training.assetmanagement.user.domain.UserFilter;
import com.training.assetmanagement.user.domain.UserProfileUpdate;
import com.training.assetmanagement.user.domain.UserStatus;
import com.training.assetmanagement.user.service.AvatarService;
import com.training.assetmanagement.user.service.DeactivationService;
import com.training.assetmanagement.user.service.UserQueryService;
import com.training.assetmanagement.user.service.UserService;
import com.training.assetmanagement.user.web.dto.request.CreateUserRequest;
import com.training.assetmanagement.user.web.dto.request.DeactivateUserRequest;
import com.training.assetmanagement.user.web.dto.request.ReactivateUserRequest;
import com.training.assetmanagement.user.web.dto.request.UpdateUserRequest;
import com.training.assetmanagement.user.web.dto.response.AvatarResponse;
import com.training.assetmanagement.user.web.dto.response.HeadReplacementCheckResponse;
import com.training.assetmanagement.user.web.dto.response.UserDeactivationCheckResponse;
import com.training.assetmanagement.user.web.dto.response.UserResponse;
import com.training.assetmanagement.user.web.mapper.UserResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(ApiPath.USERS)
@Tag(name = ApiTag.USERS)
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;
    private final UserService userService;
    private final DeactivationService deactivationService;
    private final AvatarService avatarService;

    // sort: username | fullName | createdAt; mặc định username tăng dần
    @GetMapping
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "listUsers", summary = "Search users with filters and paging")
    public PageResponse<UserResponse> list(@RequestParam(required = false) String q,
                                           @RequestParam(required = false) UUID orgUnitId,
                                           @RequestParam(required = false) UUID roleId,
                                           @RequestParam(required = false) UserStatus status,
                                           @ParameterObject @PageableDefault(size = PageDefaults.DEFAULT_SIZE)
                                           Pageable pageable) {
        return PageResponse.from(userQueryService.list(new UserFilter(q, orgUnitId, roleId, status), pageable),
            UserResponseMapper::toResponse);
    }

    @GetMapping(ApiPath.BY_ID)
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "getUser", summary = "Get a user account")
    public UserResponse get(@PathVariable UUID id) {
        return UserResponseMapper.toResponse(userQueryService.get(id));
    }

    @PostMapping
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "createUser", summary = "Create a user and email the temporary password")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponseMapper.toResponse(userService.create(new NewUser(request.username(), request.email(),
            request.fullName(), request.phoneNumber(), request.orgUnitId(), request.rankId(), request.roleIds())));
    }

    @PutMapping(ApiPath.BY_ID)
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "updateUser", summary = "Update a user, replacing heads of units they leave")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponseMapper.toResponse(userService.update(id, new UserProfileUpdate(request.fullName(),
            request.phoneNumber(), request.orgUnitId(), request.rankId(), request.roleIds(),
            UserResponseMapper.toReplacements(request.headReplacements()))));
    }

    @GetMapping(ApiPath.UNIT_CHANGE_CHECK)
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "checkUserUnitChange", summary = "List units needing a new head if the user moves")
    public HeadReplacementCheckResponse checkUnitChange(@PathVariable UUID id, @RequestParam UUID orgUnitId) {
        return UserResponseMapper.toUnitChangeCheck(userService.checkUnitChange(id, orgUnitId));
    }

    @GetMapping(ApiPath.DEACTIVATION_CHECK)
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "checkUserDeactivation", summary = "List what blocks deactivating the user")
    public UserDeactivationCheckResponse checkDeactivation(@PathVariable UUID id) {
        return UserResponseMapper.toDeactivationCheck(deactivationService.check(id));
    }

    @PostMapping(ApiPath.DEACTIVATE)
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "deactivateUser", summary = "Deactivate a user and revoke their sessions")
    public void deactivate(@PathVariable UUID id, @Valid @RequestBody DeactivateUserRequest request) {
        deactivationService.deactivate(id, UserResponseMapper.toReplacements(request.headReplacements()));
    }

    @PostMapping(ApiPath.REACTIVATE)
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "reactivateUser", summary = "Reactivate a user into an active department")
    public void reactivate(@PathVariable UUID id, @Valid @RequestBody ReactivateUserRequest request) {
        deactivationService.reactivate(id, request.orgUnitId());
    }

    @PutMapping(value = ApiPath.USER_AVATAR, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "changeUserAvatar", summary = "Change a user's avatar")
    public AvatarResponse changeAvatar(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return new AvatarResponse(avatarService.changeAvatar(id, file));
    }

    @DeleteMapping(ApiPath.USER_AVATAR)
    @PreAuthorize(ApiAuthority.USER_MANAGE)
    @Operation(operationId = "removeUserAvatar", summary = "Remove a user's avatar")
    public void removeAvatar(@PathVariable UUID id) {
        avatarService.removeAvatar(id);
    }
}
