package com.king.aicustomerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.king.aicustomerservice.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车 Mapper 接口
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
