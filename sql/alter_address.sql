-- 已有库增量添加收货地址表（不必重建整个库）
-- mysql -u root -proot123456 -P 3306 customer_service < sql/alter_address.sql

USE customer_service;

CREATE TABLE IF NOT EXISTS user_address (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    user_id        BIGINT       NOT NULL COMMENT '用户ID',
    receiver_name  VARCHAR(50)  NOT NULL COMMENT '收货人',
    receiver_phone VARCHAR(20)  NOT NULL COMMENT '手机号',
    province       VARCHAR(50)  DEFAULT NULL COMMENT '省',
    city           VARCHAR(50)  DEFAULT NULL COMMENT '市',
    district       VARCHAR(50)  DEFAULT NULL COMMENT '区/县',
    detail         VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认: 0否 1是',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

INSERT INTO user_address (user_id, receiver_name, receiver_phone, province, city, district, detail, is_default)
SELECT 2, '小明', '13800001111', '广东省', '深圳市', '南山区', '科技园路 1 号', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_address WHERE user_id = 2);

INSERT INTO user_address (user_id, receiver_name, receiver_phone, province, city, district, detail, is_default)
SELECT 2, '小明父母', '13900002222', '广东省', '广州市', '天河区', '天河路 88 号', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_address WHERE user_id = 2 AND receiver_phone = '13900002222');
