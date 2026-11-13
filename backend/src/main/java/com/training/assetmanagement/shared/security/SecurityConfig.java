package com.training.assetmanagement.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Khớp hash BCrypt cost 10 không tiền tố của seed V7 - không dùng DelegatingPasswordEncoder
    private static final int BCRYPT_STRENGTH = 10;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler,
                                                   JwtService jwtService,
                                                   ActorAuthenticator actorAuthenticator) throws Exception {
        // Tạo tay, không phải bean - tránh bị đăng ký thêm làm servlet filter
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtService, actorAuthenticator);
        http
            .csrf(AbstractHttpConfigurer::disable)          // API stateless, không dùng cookie phiên
            .cors(AbstractHttpConfigurer::disable)          // cùng origin qua proxy - cần CORS nghĩa là proxy sai
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PublicPaths.ALL).permitAll()
                // Ba endpoint vẫn mở khi cờ bắt buộc đổi mật khẩu bật
                .requestMatchers(HttpMethod.POST, PasswordChangeGate.ALLOWED_POST_PATHS).authenticated()
                .requestMatchers(HttpMethod.GET, PasswordChangeGate.ALLOWED_GET_PATHS).authenticated()
                .anyRequest().access(PasswordChangeGate.authorizationManager()))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
}
