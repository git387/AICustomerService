# 基于Spring AI2.0的带智能客服的商城系统

一个适合入门学习的全栈商城项目，集成 Spring AI 2.0 智能客服（RAG 增强）、支付宝沙箱支付、Redis 向量数据库。

## 技术栈

| 模块 | 技术 |
|------|------|
| 后端 | Spring Boot 4.1 + Spring AI 2.0 + MyBatis-Plus + Spring Security |
| 管理端 | Vue 3 + Element Plus + ECharts + Vite |
| 用户端 | Thymeleaf 模板引擎 |
| 数据库 | MySQL 5.7 (端口 3306) |
| 向量库 | Redis Stack（需 RediSearch 模块） |
| 大模型 | 阿里云 DashScope（OpenAI 兼容接口） |
| 支付 | 支付宝沙箱 |

## 项目结构

```
AICustomerService/          # Spring Boot 后端
├── src/main/java/          # Java 源码（含中文注释）
├── src/main/resources/
│   ├── templates/          # Thymeleaf 用户端页面
│   └── static/             # CSS / JS
├── sql/init.sql            # 建库建表及测试数据
├── sql/sample-knowledge/   # 示例知识库 FAQ
└── client/                 # Vue3 管理后台
```

## 快速开始

### 1. 环境准备

- JDK 17+（请将 `JAVA_HOME` 指向 JDK 17，本机若同时安装了 JDK 11 需注意）
- MySQL 5.7.44（端口 3306，账号 `root` / `root123456`）
- Redis Stack 6+（必须包含 RediSearch，默认 `127.0.0.1:6379` 无密码）
- Node.js 18+

### 2. 初始化数据库

```bash
mysql -u root -proot123456 -P 3306 < sql/init.sql
```

### 3. 配置密钥

编辑 `src/main/resources/application.properties`：

```properties
# 必填：阿里云百炼 API Key，否则智能客服和知识库向量化不可用
spring.ai.openai.api-key=你的API密钥

# 可选：支付宝沙箱。不填时仍可下单，但无法真实跳转付款
alipay.app-id=你的沙箱APPID
alipay.private-key=你的应用私钥
alipay.public-key=支付宝沙箱公钥
```

也可通过环境变量 `DASHSCOPE_API_KEY` 注入 API Key。

### 4. 启动后端

```bash
./mvnw spring-boot:run
```

用户端商城：http://localhost:8080

### 5. 启动管理后台

```bash
cd client
npm install
npm run dev
```

管理后台：http://localhost:5173

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | root123456 |
| 普通用户 | user1 | root123456 |

密码在数据库中以 MD5 存储（`dbb1c112a931eeb16299d9de1f30161d`）。

## 功能说明

### 用户端 (http://localhost:8080)

- 商品浏览、搜索、分类筛选
- 购物车、下单、支付宝沙箱支付
- 智能客服（RAG 知识库增强对话）

### 管理后台 (http://localhost:5173)

- 数据统计仪表盘（ECharts：近 7 天订单、状态分布）
- 商品 / 分类 / 订单 / 用户管理
- 知识库文件上传（txt / doc / pdf / markdown）及向量化

### 智能客服 RAG 流程

1. 管理员上传知识库文件（可先用 `sql/sample-knowledge/faq.txt`）
2. 系统解析文档并文本分块
3. 调用 `qwen3.7-text-embedding` 生成 2048 维向量
4. 写入 Redis 向量数据库
5. 用户提问时检索相关文档，结合 `qwen3.8-max` 生成回答

## 文件上传目录

所有上传文件统一存储在 `D:/workspace/uploads/` 下：

- `products/` 商品图片
- `knowledge/` 知识库文件

## 说明

- 无 DashScope API Key 时，商城浏览和下单仍可用，客服与向量化会提示配置错误。
- 无支付宝沙箱密钥时，下单流程可走通，支付页会提示尚未配置。
- 本项目面向入门学习，密码采用 MD5，生产环境请改用 BCrypt 等更安全算法。
