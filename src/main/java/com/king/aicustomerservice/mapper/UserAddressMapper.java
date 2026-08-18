package com.king.aicustomerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.king.aicustomerservice.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址 Mapper 接口
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
