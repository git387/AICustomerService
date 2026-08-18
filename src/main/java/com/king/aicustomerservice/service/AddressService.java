package com.king.aicustomerservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.king.aicustomerservice.entity.SysUser;
import com.king.aicustomerservice.entity.UserAddress;
import com.king.aicustomerservice.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 收货地址服务
 * 约束：同一用户默认地址最多一个；收货人+手机号+完整地址不可重复
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressMapper userAddressMapper;
    private final SysUserService sysUserService;

    /**
     * 查询指定用户的全部地址，默认地址排在前面
     */
    public List<UserAddress> listByUser(Long userId) {
        return userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getId));
    }

    /**
     * 根据ID查询地址
     */
    public UserAddress findById(Long id) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null) {
            throw new RuntimeException("收货地址不存在");
        }
        return address;
    }

    /**
     * 查询属于当前用户的地址
     */
    public UserAddress findOwned(Long id, Long userId) {
        UserAddress address = findById(id);
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该地址");
        }
        return address;
    }

    /**
     * 新增地址；首条自动设为默认，设为默认时取消该用户其他默认地址
     */
    @Transactional
    public UserAddress save(Long userId, UserAddress address) {
        normalize(address);
        validate(address);
        address.setId(null);
        address.setUserId(userId);
        assertUnique(userId, address, null);
        boolean wantDefault = markedDefault(address) || listByUser(userId).isEmpty();
        address.setIsDefault(wantDefault ? 1 : 0);
        if (wantDefault) {
            clearDefault(userId, null);
        }
        userAddressMapper.insert(address);
        return address;
    }

    /**
     * 更新地址
     */
    @Transactional
    public void update(Long userId, UserAddress address) {
        UserAddress exist = findOwned(address.getId(), userId);
        normalize(address);
        validate(address);
        address.setUserId(userId);
        assertUnique(userId, address, address.getId());
        applyDefaultOnUpdate(userId, exist, address);
        userAddressMapper.updateById(address);
    }

    /**
     * 删除地址；若删的是默认地址，则把剩余最新一条设为默认
     */
    @Transactional
    public void delete(Long userId, Long id) {
        findOwned(id, userId);
        userAddressMapper.deleteById(id);
        ensureHasDefault(userId);
    }

    /**
     * 设为默认地址，同时取消该用户其他默认标记
     */
    @Transactional
    public void setDefault(Long userId, Long id) {
        findOwned(id, userId);
        clearDefault(userId, id);
        UserAddress address = new UserAddress();
        address.setId(id);
        address.setIsDefault(1);
        userAddressMapper.updateById(address);
    }

    /**
     * 管理端分页查询全部地址
     */
    public Page<UserAddress> pageForAdmin(int page, int size, Long userId, String keyword) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<UserAddress>()
                .eq(userId != null, UserAddress::getUserId, userId)
                .and(StringUtils.hasText(keyword), q -> q
                        .like(UserAddress::getReceiverName, keyword)
                        .or().like(UserAddress::getReceiverPhone, keyword)
                        .or().like(UserAddress::getDetail, keyword))
                .orderByDesc(UserAddress::getId);
        Page<UserAddress> result = userAddressMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(item -> {
            SysUser user = sysUserService.findById(item.getUserId());
            if (user != null) {
                item.setUsername(user.getUsername());
            }
        });
        return result;
    }

    /**
     * 管理员新增地址
     */
    @Transactional
    public UserAddress saveByAdmin(UserAddress address) {
        if (address.getUserId() == null) {
            throw new RuntimeException("请选择用户");
        }
        if (sysUserService.findById(address.getUserId()) == null) {
            throw new RuntimeException("用户不存在");
        }
        return save(address.getUserId(), address);
    }

    /**
     * 管理员修改地址
     */
    @Transactional
    public void updateByAdmin(UserAddress address) {
        UserAddress exist = findById(address.getId());
        address.setUserId(exist.getUserId());
        update(exist.getUserId(), address);
    }

    /**
     * 管理员删除地址
     */
    @Transactional
    public void deleteByAdmin(Long id) {
        UserAddress exist = findById(id);
        userAddressMapper.deleteById(id);
        ensureHasDefault(exist.getUserId());
    }

    /**
     * 更新时处理默认地址：新设默认则取消其他；不能把唯一默认取消掉
     */
    private void applyDefaultOnUpdate(Long userId, UserAddress exist, UserAddress incoming) {
        boolean wantDefault = markedDefault(incoming);
        if (wantDefault) {
            clearDefault(userId, exist.getId());
            incoming.setIsDefault(1);
            return;
        }
        incoming.setIsDefault(0);
        if (markedDefault(exist) && !hasOtherDefault(userId, exist.getId())) {
            throw new RuntimeException("每个用户只能保留一个默认地址，请先将其他地址设为默认后再取消");
        }
    }

    /**
     * 删除后若没有任何默认地址，则将最新一条设为默认
     */
    private void ensureHasDefault(Long userId) {
        List<UserAddress> list = listByUser(userId);
        if (list.isEmpty()) {
            return;
        }
        boolean hasDefault = list.stream().anyMatch(this::markedDefault);
        if (!hasDefault) {
            setDefault(userId, list.get(0).getId());
        }
    }

    private boolean hasOtherDefault(Long userId, Long excludeId) {
        return userAddressMapper.selectCount(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .ne(UserAddress::getId, excludeId)) > 0;
    }

    /**
     * 取消该用户其他地址的默认标记
     */
    private void clearDefault(Long userId, Long excludeId) {
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .ne(excludeId != null, UserAddress::getId, excludeId)
                .set(UserAddress::getIsDefault, 0);
        userAddressMapper.update(null, wrapper);
    }

    /**
     * 同一用户下，收货人+手机号+完整地址相同则视为重复
     */
    private void assertUnique(Long userId, UserAddress address, Long excludeId) {
        String incomingKey = contentKey(address);
        for (UserAddress exist : listByUser(userId)) {
            if (excludeId != null && excludeId.equals(exist.getId())) {
                continue;
            }
            if (incomingKey.equals(contentKey(exist))) {
                throw new RuntimeException("该收货地址已存在，请勿重复保存");
            }
        }
    }

    private boolean markedDefault(UserAddress address) {
        return address.getIsDefault() != null && address.getIsDefault() == 1;
    }

    /**
     * 去掉首尾空格，避免“相同地址”因空格被当成两条
     */
    private void normalize(UserAddress address) {
        address.setReceiverName(trimToEmpty(address.getReceiverName()));
        address.setReceiverPhone(trimToEmpty(address.getReceiverPhone()));
        address.setProvince(trimToEmpty(address.getProvince()));
        address.setCity(trimToEmpty(address.getCity()));
        address.setDistrict(trimToEmpty(address.getDistrict()));
        address.setDetail(trimToEmpty(address.getDetail()));
    }

    private String contentKey(UserAddress address) {
        return compact(address.getReceiverName())
                + "|" + compact(address.getReceiverPhone())
                + "|" + compact(address.fullAddress());
    }

    private String compact(String value) {
        return trimToEmpty(value).replaceAll("\\s+", "");
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 校验必填项
     */
    private void validate(UserAddress address) {
        if (!StringUtils.hasText(address.getReceiverName())) {
            throw new RuntimeException("请填写收货人");
        }
        if (!StringUtils.hasText(address.getReceiverPhone())) {
            throw new RuntimeException("请填写手机号");
        }
        if (!StringUtils.hasText(address.getDetail())) {
            throw new RuntimeException("请填写详细地址");
        }
    }
}
