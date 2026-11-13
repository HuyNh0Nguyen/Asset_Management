package com.training.assetmanagement.auth.service;

import com.training.assetmanagement.auth.domain.LoginOutcome;

// Bean riêng: Gọi nội bộ trong cùng bean thì proxy giao dịch không chạy
public interface LoginAttemptService {

    // Một giao dịch; luôn commit bộ đếm + Audit rồi mới trả kêt quả
    LoginOutcome attempt(String username, String rawPassword);
}
