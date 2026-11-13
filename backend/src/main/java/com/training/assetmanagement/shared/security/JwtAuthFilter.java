package com.training.assetmanagement.shared.security;

import com.training.assetmanagement.shared.web.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

// Mỗi request: Chữ ký + exp -> iat >= token_valid_from -> ACTIVE -> nạp quyền.
// KHÔNG gắn @Component: Spring Boot sẽ đăng ký thêm làm servlet filter và chạy hai lần
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ActorAuthenticator actorAuthenticator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            authenticate(request, header.substring(BEARER_PREFIX.length()).trim());
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        Optional<Authentication> authentication = jwtService.verify(token)
            .flatMap(verified -> actorAuthenticator.authenticate(verified.userId(), verified.issuedAt()));
        if (authentication.isPresent()) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication.get());
            SecurityContextHolder.setContext(context);
            return;
        }
        // Đi tiếp chưa xác thực: Đường dẫn công khai vẫn chạy, còn lại entry point viết 401
        AuthFailure.mark(request, ErrorCode.SESSION_EXPIRED);
    }
}
