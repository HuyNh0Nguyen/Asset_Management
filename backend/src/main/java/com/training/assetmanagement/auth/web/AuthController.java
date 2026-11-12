package com.training.assetmanagement.auth.web;

import com.training.assetmanagement.auth.web.dto.request.ChangePasswordRequest;
import com.training.assetmanagement.auth.web.dto.request.LoginRequest;
import com.training.assetmanagement.auth.web.dto.response.LoginResponse;
import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Chữ ký trước, hiện thực ở phase 2 (khi đó gọi AuthService / AuthServiceImpl)
@RestController
@RequestMapping(ApiPath.AUTH)
@Tag(name = ApiTag.AUTH)
public class AuthController {

    @PostMapping(ApiPath.LOGIN)
    @Operation(operationId = "login", summary = "Authenticate user and return access token")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @PostMapping(ApiPath.LOGOUT)
    @Operation(operationId = "logout", summary = "Invalidate user session and access token")
    public void logout() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @PostMapping(ApiPath.CHANGE_PASSWORD)
    @Operation(operationId = "changePassword", summary = "Change user password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
