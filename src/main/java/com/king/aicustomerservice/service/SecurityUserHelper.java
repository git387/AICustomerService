package com.king.aicustomerservice.service;

import com.king.aicustomerservice.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全上下文工具
 * 从当前登录会话中取出用户信息
 */
@Component
public class SecurityUserHelper {

    private final SysUserService sysUserService;

    public SecurityUserHelper(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 获取当前登录用户，未登录则抛出异常
     */
    public SysUser requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("请先登录");
        }
        SysUser user = sysUserService.findByUsername(authentication.getName());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    /**
     * 获取当前登录用户，未登录返回 null
     */
    public SysUser currentUserOrNull() {
        try {
            return requireUser();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
