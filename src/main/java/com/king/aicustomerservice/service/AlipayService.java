package com.king.aicustomerservice.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.king.aicustomerservice.config.AlipayConfig;
import com.king.aicustomerservice.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝沙箱支付服务
 * 生成电脑网站支付表单，并校验异步通知签名
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    private final AlipayConfig alipayConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 判断沙箱密钥是否已配置完整
     */
    public boolean isConfigured() {
        return hasText(alipayConfig.getAppId())
                && !alipayConfig.getAppId().startsWith("your-")
                && hasText(alipayConfig.getPrivateKey())
                && !alipayConfig.getPrivateKey().startsWith("your-")
                && hasText(alipayConfig.getPublicKey())
                && !alipayConfig.getPublicKey().startsWith("your-");
    }

    /**
     * 生成电脑网站支付 HTML 表单
     */
    public String buildPayForm(Order order) {
        if (!isConfigured()) {
            throw new RuntimeException("尚未配置支付宝沙箱密钥，请在 application.properties 中填写 alipay.app-id / private-key / public-key");
        }
        String gatewayUrl = hasText(alipayConfig.getGatewayUrl())
                ? alipayConfig.getGatewayUrl().trim()
                : "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
        String privateKey = alipayConfig.getPrivateKey().replaceAll("\\s+", "");
        String publicKey = alipayConfig.getPublicKey().replaceAll("\\s+", "");
        String notifyUrl = alipayConfig.getNotifyUrl();
        String returnUrl = alipayConfig.getReturnUrl();
        if (!hasText(notifyUrl) || !hasText(returnUrl)) {
            throw new RuntimeException("请配置 alipay.notify-url 和 alipay.return-url");
        }
        try {
            AlipayClient client = new DefaultAlipayClient(
                    gatewayUrl,
                    alipayConfig.getAppId().trim(),
                    privateKey,
                    "json",
                    "UTF-8",
                    publicKey,
                    "RSA2"
            );
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(notifyUrl.trim());
            request.setReturnUrl(returnUrl.trim());
            Map<String, Object> biz = new LinkedHashMap<>();
            biz.put("out_trade_no", order.getOrderNo());
            biz.put("total_amount", order.getTotalAmount().toPlainString());
            biz.put("subject", "商城订单-" + order.getOrderNo());
            biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
            request.setBizContent(objectMapper.writeValueAsString(biz));
            return client.pageExecute(request).getBody();
        } catch (Exception e) {
            log.error("生成支付宝表单失败", e);
            throw new RuntimeException("发起支付宝支付失败: " + e.getMessage());
        }
    }

    /**
     * 校验支付宝异步通知签名
     */
    public boolean verifyNotify(Map<String, String> params) {
        if (!isConfigured()) {
            return false;
        }
        try {
            return AlipaySignature.rsaCheckV1(params, alipayConfig.getPublicKey(), "UTF-8", "RSA2");
        } catch (Exception e) {
            log.error("支付宝验签失败", e);
            return false;
        }
    }

    /**
     * 判断字符串非空
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
