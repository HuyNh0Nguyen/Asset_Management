package com.training.assetmanagement.auth.service;

import com.training.assetmanagement.auth.domain.LoginOutcome;
import com.training.assetmanagement.auth.domain.LoginResult;
import com.training.assetmanagement.shared.security.JwtService;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Facade KHÔNG giao dịch: Lỗi ném ở đây, sau khi attempt() đã commit bộ đếm + Audit
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LoginAttemptService loginAttemptService;
    private final JwtService jwtService;

    @Override
    public LoginResult login(String username, String password) {
        LoginOutcome outcome = loginAttemptService.attempt(username, password);
        if (outcome instanceof LoginOutcome.Success success) {
            return new LoginResult(jwtService.issue(success.userId()), success.mustChangePassword());
        }
        throw new BusinessException(toErrorCode(outcome));
    }

    private static ErrorCode toErrorCode(LoginOutcome outcome) {
        if (outcome instanceof LoginOutcome.Locked) {
            return ErrorCode.ACCOUNT_LOCKED;
        }
        if (outcome instanceof LoginOutcome.Disabled) {
            return ErrorCode.ACCOUNT_DISABLED;
        }
        return ErrorCode.INVALID_CREDENTIALS;
    }
}
