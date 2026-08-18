package com.king.aicustomerservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.king.aicustomerservice.entity.SysUser;
import com.king.aicustomerservice.mapper.SysUserMapper;
import com.king.aicustomerservice.util.Md5Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 系统用户服务类
 * 处理用户注册、登录、查询等业务
 */
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;

    /**
     * 根据用户名查询用户
     */
    public SysUser findByUsername(String username) {
        return sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    /**
     * 根据ID查询用户
     */
    public SysUser findById(Long id) {
        return sysUserMapper.selectById(id);
    }

    /**
     * 用户注册
     */
    public SysUser register(String username, String password, String nickname) {
        if (findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(Md5Util.encrypt(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setRole("USER");
        user.setStatus(1);
        sysUserMapper.insert(user);
        return user;
    }

    /**
     * 分页查询用户列表
     */
    public Page<SysUser> pageList(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysUser::getUsername, keyword)
                   .or().like(SysUser::getNickname, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));
        return result;
    }

    /**
     * 更新用户信息
     */
    public void update(SysUser user) {
        sysUserMapper.updateById(user);
    }

    /**
     * 删除用户
     */
    public void delete(Long id) {
        sysUserMapper.deleteById(id);
    }

    /**
     * 统计用户总数
     */
    public long count() {
        return sysUserMapper.selectCount(null);
    }
}
