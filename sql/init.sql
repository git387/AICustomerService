-- 基于Spring AI2.0的带智能客服的商城系统
-- MySQL 5.7.44  端口 3306  库名 customer_service
-- 执行方式: mysql -u root -proot123456 -P 3306 < sql/init.sql

CREATE DATABASE IF NOT EXISTS customer_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE customer_service;

-- ----------------------------
-- 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS knowledge_file;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS user_address;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录名',
    password    VARCHAR(64)  NOT NULL COMMENT 'MD5加密后的密码',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN / USER',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ----------------------------
-- 商品分类
-- ----------------------------
CREATE TABLE category (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    sort_order  INT          DEFAULT 0 COMMENT '排序号，越小越靠前',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

-- ----------------------------
-- 商品
-- ----------------------------
CREATE TABLE product (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    name        VARCHAR(100) NOT NULL COMMENT '商品名称',
    category_id BIGINT       NOT NULL COMMENT '分类ID',
    price       DECIMAL(10,2) NOT NULL COMMENT '售价',
    stock       INT          NOT NULL DEFAULT 0 COMMENT '库存',
    image       VARCHAR(255) DEFAULT NULL COMMENT '主图路径',
    description TEXT         COMMENT '商品简介',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0下架 1上架',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

-- ----------------------------
-- 购物车
-- ----------------------------
CREATE TABLE cart_item (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车项ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    product_id  BIGINT NOT NULL COMMENT '商品ID',
    quantity    INT    NOT NULL DEFAULT 1 COMMENT '数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- ----------------------------
-- 收货地址
-- ----------------------------
CREATE TABLE user_address (
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

-- ----------------------------
-- 订单
-- ----------------------------
CREATE TABLE orders (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no        VARCHAR(32)  NOT NULL UNIQUE COMMENT '订单号',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    total_amount    DECIMAL(10,2) NOT NULL COMMENT '订单总额',
    status          VARCHAR(20)  NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID/SHIPPED/COMPLETED/CANCELLED',
    receiver_name   VARCHAR(50)  DEFAULT NULL COMMENT '收货人',
    receiver_phone  VARCHAR(20)  DEFAULT NULL COMMENT '收货电话',
    address         VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    alipay_trade_no VARCHAR(64)  DEFAULT NULL COMMENT '支付宝交易号',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    pay_time        DATETIME     DEFAULT NULL COMMENT '支付时间',
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- ----------------------------
-- 订单明细
-- ----------------------------
CREATE TABLE order_item (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    order_id      BIGINT        NOT NULL COMMENT '订单ID',
    product_id    BIGINT        NOT NULL COMMENT '商品ID',
    product_name  VARCHAR(100)  NOT NULL COMMENT '商品快照名称',
    product_image VARCHAR(255)  DEFAULT NULL COMMENT '商品快照图片',
    price         DECIMAL(10,2) NOT NULL COMMENT '成交单价',
    quantity      INT           NOT NULL COMMENT '购买数量',
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

-- ----------------------------
-- 知识库文件
-- ----------------------------
CREATE TABLE knowledge_file (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    original_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    stored_path   VARCHAR(255) NOT NULL COMMENT '磁盘存储路径',
    file_type     VARCHAR(20)  NOT NULL COMMENT '文件类型 txt/doc/pdf/md',
    chunk_count   INT          DEFAULT 0 COMMENT '分块数量',
    vector_ids    TEXT         COMMENT 'Redis向量文档ID列表，逗号分隔',
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAIL',
    error_msg     VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    upload_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文件';

-- ----------------------------
-- 客服聊天记录
-- ----------------------------
CREATE TABLE chat_message (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    role        VARCHAR(20)  NOT NULL COMMENT 'user / assistant',
    content     TEXT         NOT NULL COMMENT '消息内容',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    INDEX idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能客服聊天记录';

-- ----------------------------
-- 测试账号 密码均为 root123456 的 MD5
-- ----------------------------
INSERT INTO sys_user (username, password, nickname, role, status) VALUES
('admin', 'dbb1c112a931eeb16299d9de1f30161d', '系统管理员', 'ADMIN', 1),
('user1', 'dbb1c112a931eeb16299d9de1f30161d', '小明', 'USER', 1);

-- ----------------------------
-- 分类测试数据
-- ----------------------------
INSERT INTO category (name, description, sort_order, status) VALUES
('数码配件', '耳机、充电器、移动电源等', 1, 1),
('服装鞋帽', '日常穿搭与运动鞋', 2, 1),
('家居生活', '杯子、台灯、收纳用品', 3, 1),
('食品饮料', '零食、茶叶、坚果', 4, 1);

-- ----------------------------
-- 商品测试数据
-- ----------------------------
INSERT INTO product (name, category_id, price, stock, image, description, status) VALUES
('无线蓝牙耳机', 1, 129.00, 80, NULL, '入耳式蓝牙耳机，续航约 6 小时，支持通话降噪。', 1),
('65W 氮化镓充电器', 1, 89.00, 60, NULL, '三口快充，体积小巧，适合笔记本和手机同时充电。', 1),
('10000mAh 移动电源', 1, 79.00, 100, NULL, '双向快充移动电源，轻薄便携。', 1),
('纯棉圆领T恤', 2, 59.00, 120, NULL, '100% 棉，多色可选，适合日常休闲。', 1),
('轻便运动鞋', 2, 199.00, 50, NULL, '透气网面，缓震鞋底，适合慢跑和通勤。', 1),
('棒球帽', 2, 39.00, 90, NULL, '可调节帽围，防晒遮阳。', 1),
('保温马克杯', 3, 49.00, 70, NULL, '304 不锈钢内胆，保温约 6 小时。', 1),
('LED 护眼台灯', 3, 119.00, 40, NULL, '三挡调光，无频闪，适合阅读办公。', 1),
('桌面收纳盒', 3, 29.00, 150, NULL, '多层分隔，整理文具和数据线。', 1),
('混合坚果礼盒', 4, 68.00, 80, NULL, '每日坚果 30 包，含核桃、杏仁、腰果。', 1),
('茉莉花茶 150g', 4, 45.00, 60, NULL, '浓香型花茶，独立小包装。', 1);

-- ----------------------------
-- 收货地址测试数据
-- ----------------------------
INSERT INTO user_address (user_id, receiver_name, receiver_phone, province, city, district, detail, is_default) VALUES
(2, '小明', '13800001111', '广东省', '深圳市', '南山区', '科技园路 1 号', 1),
(2, '小明父母', '13900002222', '广东省', '广州市', '天河区', '天河路 88 号', 0);

-- ----------------------------
-- 订单测试数据（覆盖近 7 天与多种状态，方便仪表盘出图）
-- ----------------------------
INSERT INTO orders (order_no, user_id, total_amount, status, receiver_name, receiver_phone, address, create_time, pay_time) VALUES
('20260811001', 2, 129.00, 'COMPLETED', '小明', '13800001111', '广东省深圳市南山区科技园路 1 号', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('20260812001', 2, 268.00, 'SHIPPED',   '小明', '13800001111', '广东省深圳市南山区科技园路 1 号', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('20260813001', 2, 89.00,  'PAID',      '小明', '13800001111', '广东省深圳市南山区科技园路 1 号', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('20260814001', 2, 59.00,  'PAID',      '小明', '13800001111', '广东省深圳市南山区科技园路 1 号', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('20260815001', 2, 168.00, 'COMPLETED', '小明', '13800001111', '广东省深圳市南山区科技园路 1 号', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('20260816001', 2, 49.00,  'UNPAID',    '小明', '13800001111', '广东省深圳市南山区科技园路 1 号', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),
('20260817001', 2, 199.00, 'PAID',      '小明', '13800001111', '广东省深圳市南山区科技园路 1 号', NOW(), NOW());

INSERT INTO order_item (order_id, product_id, product_name, price, quantity) VALUES
(1, 1, '无线蓝牙耳机', 129.00, 1),
(2, 5, '轻便运动鞋', 199.00, 1),
(2, 6, '棒球帽', 39.00, 1),
(2, 9, '桌面收纳盒', 29.00, 1),
(3, 2, '65W 氮化镓充电器', 89.00, 1),
(4, 4, '纯棉圆领T恤', 59.00, 1),
(5, 8, 'LED 护眼台灯', 119.00, 1),
(5, 7, '保温马克杯', 49.00, 1),
(6, 7, '保温马克杯', 49.00, 1),
(7, 5, '轻便运动鞋', 199.00, 1);
