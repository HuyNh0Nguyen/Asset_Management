package com.training.assetmanagement.shared.security;

import com.training.assetmanagement.shared.web.ApiPath;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

// Tài khoản đang bật cờ bắt buộc đổi mật khẩu chỉ gọi được ba endpoint
public final class PasswordChangeGate {

    public static final String[] ALLOWED_POST_PATHS = {ApiPath.AUTH_CHANGE_PASSWORD, ApiPath.AUTH_LOGOUT};
    public static final String[] ALLOWED_GET_PATHS = {ApiPath.ME};

    // Gắn vào anyRequest(): Đã đăng nhập VÀ không mang PASSWORD_CHANGE_REQUIRED
    public static AuthorizationManager<RequestAuthorizationContext> authorizationManager() {
        return AuthorizationManagers.<RequestAuthorizationContext>allOf(
            AuthenticatedAuthorizationManager.authenticated(),
            AuthorizationManagers.not(
                AuthorityAuthorizationManager.hasAuthority(SecurityAuthority.PASSWORD_CHANGE_REQUIRED.name())
            )
        );
    }

    // Handler dùng để tách 403 của cổng khỏi 403 thường
    public static boolean isPending(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> SecurityAuthority.PASSWORD_CHANGE_REQUIRED.name().equals(authority.getAuthority()));
    }

    private PasswordChangeGate() {}
}
