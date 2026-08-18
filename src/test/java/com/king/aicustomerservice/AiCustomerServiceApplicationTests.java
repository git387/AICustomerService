package com.king.aicustomerservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 应用上下文加载测试（需要本地 MySQL 与 Redis）
 */
@SpringBootTest
@Disabled("需要本地 MySQL 3306 与 Redis Stack")
class AiCustomerServiceApplicationTests {

    /**
     * 验证 Spring 容器能够启动
     */
    @Test
    void contextLoads() {
    }
}
