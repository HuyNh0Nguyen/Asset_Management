package com.training.assetmanagement.shared.security;

import java.util.Optional;
import java.util.UUID;

public interface JwtService {

    // Phát token HS256: Sub = id người dùng, iat, exp
    String issue(UUID userId);

    // Kiểm chữ ký + exp; hỏng -> rỗng
    Optional<VerifiedToken> verify(String token);
}
