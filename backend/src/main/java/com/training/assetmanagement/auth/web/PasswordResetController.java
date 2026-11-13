package com.training.assetmanagement.auth.web;

import com.training.assetmanagement.auth.service.PasswordService;
import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// Tiền tố /users nhưng nằm ở auth.web: web chỉ gọi service cùng miền
// Tag USERS -> FE nhận trong UsersService
@RestController
@RequestMapping(ApiPath.USERS)
@Tag(name = ApiTag.USERS)
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordService passwordService;

    @PostMapping(ApiPath.RESET_PASSWORD)
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(operationId = "resetUserPassword", summary = "Reset a user's password")
    public void resetPassword(@PathVariable UUID id) {
        passwordService.resetPassword(id);
    }
}
