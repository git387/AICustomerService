package com.king.aicustomerservice.controller;

import com.king.aicustomerservice.common.Result;
import com.king.aicustomerservice.entity.SysUser;
import com.king.aicustomerservice.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录注册接口
 * 管理端与用户端共用 Session
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SysUserService sysUserService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException("用户名或密码错误");
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        SysUser user = sysUserService.findByUsername(request.getUsername());
        user.setPassword(null);
        return Result.ok("登录成功", user);
    }

    /**
     * 用户注册（仅普通用户角色）
     */
    @PostMapping("/register")
    public Result<SysUser> register(@RequestBody RegisterRequest request) {
        SysUser user = sysUserService.register(request.getUsername(), request.getPassword(), request.getNickname());
        user.setPassword(null);
        return Result.ok("注册成功", user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.ok("已退出", null);
    }

    /**
     * 当前登录用户
     */
    @GetMapping("/me")
    public Result<SysUser> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.ok(null);
        }
        SysUser user = sysUserService.findByUsername(authentication.getName());
        if (user != null) {
            user.setPassword(null);
        }
        return Result.ok(user);
    }

    /**
     * 登录请求体
     */
    @Data
    public static class LoginRequest {
        /** 用户名 */
        private String username;
        /** 明文密码 */
        private String password;
    }

    /**
     * 注册请求体
     */
    @Data
    public static class RegisterRequest {
        /** 用户名 */
        private String username;
        /** 明文密码 */
        private String password;
        /** 昵称 */
        private String nickname;
    }
}
