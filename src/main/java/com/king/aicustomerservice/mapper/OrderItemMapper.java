package com.king.aicustomerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.king.aicustomerservice.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper 接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
