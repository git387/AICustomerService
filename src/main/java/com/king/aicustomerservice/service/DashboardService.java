package com.king.aicustomerservice.service;

import com.king.aicustomerservice.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台仪表盘统计服务
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderMapper orderMapper;
    private final SysUserService sysUserService;
    private final ProductService productService;
    private final OrderService orderService;

    /**
     * 汇总首页统计数据：卡片指标 + 近7天订单 + 订单状态分布
     */
    public Map<String, Object> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", sysUserService.count());
        data.put("productCount", productService.count());
        data.put("orderCount", orderService.count());
        BigDecimal sales = orderMapper.sumTotalSales();
        data.put("totalSales", sales == null ? BigDecimal.ZERO : sales);
        data.put("orderTrend", orderMapper.countLast7Days());
        List<Map<String, Object>> statusList = orderMapper.countByStatus();
        data.put("orderStatus", statusList);
        return data;
    }
}
