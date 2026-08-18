package com.king.aicustomerservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝沙箱支付配置类
 * 请在 application.properties 中配置你的沙箱密钥
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /** 沙箱应用APPID */
    private String appId;

    /** 应用私钥 */
    private String privateKey;

    /** 支付宝公钥 */
    private String publicKey;

    /** 网关地址，默认支付宝沙箱网关 */
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /** 异步通知地址 */
    private String notifyUrl;

    /** 同步跳转地址（浏览器回跳，本地开发请用 localhost） */
    private String returnUrl = "http://localhost:8080/pay/return";
}
