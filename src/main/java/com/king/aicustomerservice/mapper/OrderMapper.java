package com.king.aicustomerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.king.aicustomerservice.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 统计各状态订单数量
     */
    @Select("SELECT status, COUNT(*) as count FROM orders GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 统计总销售额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE status IN ('PAID','SHIPPED','COMPLETED')")
    BigDecimal sumTotalSales();

    /**
     * 按日期统计近7天订单数
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') as date, COUNT(*) as count FROM orders " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY date")
    List<Map<String, Object>> countLast7Days();
}
