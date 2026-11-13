package com.training.assetmanagement.auth.service;

import com.training.assetmanagement.auth.domain.LoginResult;

public interface AuthService {

    // Sai -> BusinessException 401; đúng -> JWT
    LoginResult login(String username, String password);
}
