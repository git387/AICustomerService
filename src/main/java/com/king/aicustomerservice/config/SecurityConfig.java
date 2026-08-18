package com.king.aicustomerservice.config;

import com.king.aicustomerservice.entity.SysUser;
import com.king.aicustomerservice.service.SysUserService;
import com.king.aicustomerservice.util.Md5Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置
 * 区分管理员与普通用户，使用 Session + MD5 密码校验
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SysUserService sysUserService;

    /**
     * 安全过滤器链：公开浏览接口，管理端仅 ADMIN 可访问
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/", "/login", "/register", "/product/**",
                                "/css/**", "/js/**", "/uploads/**", "/error", "/favicon.ico",
                                "/api/auth/**", "/api/products/**", "/api/categories",
                                "/pay/notify", "/pay/return"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::handleUnauthorized)
                        .accessDeniedHandler(this::handleAccessDenied)
                );
        return http.build();
    }

    /**
     * 未登录处理：接口返回 JSON，页面跳转登录
     */
    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                    org.springframework.security.core.AuthenticationException ex) throws java.io.IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
        } else {
            String target = request.getRequestURI();
            String query = request.getQueryString();
            if (query != null && !query.isBlank()) {
                target = target + "?" + query;
            }
            String redirect = java.net.URLEncoder.encode(target, java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect("/login?redirect=" + redirect);
        }
    }

    /**
     * 无权限处理
     */
    private void handleAccessDenied(HttpServletRequest request, HttpServletResponse response,
                                    org.springframework.security.access.AccessDeniedException ex) throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
    }

    /**
     * 从数据库加载用户，禁用账号禁止登录
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            SysUser sysUser = sysUserService.findByUsername(username);
            if (sysUser == null) {
                throw new UsernameNotFoundException("用户不存在");
            }
            if (sysUser.getStatus() != null && sysUser.getStatus() == 0) {
                throw new UsernameNotFoundException("账号已被禁用");
            }
            return User.withUsername(sysUser.getUsername())
                    .password(sysUser.getPassword())
                    .roles(sysUser.getRole())
                    .build();
        };
    }

    /**
     * 学习项目按要求使用 MD5 作为密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return Md5Util.encrypt(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encode(rawPassword).equalsIgnoreCase(encodedPassword);
            }
        };
    }

    /**
     * 认证管理器，供登录接口调用
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * 允许管理端 Vite 开发服务器携带 Cookie
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
