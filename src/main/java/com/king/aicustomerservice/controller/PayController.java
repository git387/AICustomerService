package com.king.aicustomerservice.controller;

import com.king.aicustomerservice.entity.Order;
import com.king.aicustomerservice.entity.SysUser;
import com.king.aicustomerservice.service.AlipayService;
import com.king.aicustomerservice.service.OrderService;
import com.king.aicustomerservice.service.SecurityUserHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝沙箱支付控制器
 */
@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final AlipayService alipayService;
    private final OrderService orderService;
    private final SecurityUserHelper securityUserHelper;

    /**
     * 发起电脑网站支付，返回自动提交的表单页面
     */
    @GetMapping("/{orderNo}")
    public String pay(@PathVariable String orderNo, Model model) {
        SysUser user = securityUserHelper.requireUser();
        Order order = orderService.findByOrderNo(orderNo);
        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("无权支付该订单");
        }
        if (!order.getStatus().canPay()) {
            model.addAttribute("success", order.getStatus().isPaidSuccess());
            model.addAttribute("message", "订单当前状态为 " + order.getStatus().getLabel() + "，无需重复支付");
            model.addAttribute("orderNo", orderNo);
            return "pay/result";
        }
        try {
            String form = alipayService.buildPayForm(order);
            model.addAttribute("form", form);
            return "pay/alipay";
        } catch (RuntimeException e) {
            model.addAttribute("success", false);
            model.addAttribute("message", e.getMessage());
            model.addAttribute("orderNo", orderNo);
            return "pay/result";
        }
    }

    /**
     * 支付宝同步跳转（沙箱可能用 GET 或 POST）
     * 更新订单后立刻回到「我的订单」
     */
    @RequestMapping(value = "/return", method = {RequestMethod.GET, RequestMethod.POST})
    public String payReturn(HttpServletRequest request) {
        String orderNo = request.getParameter("out_trade_no");
        String tradeNo = request.getParameter("trade_no");
        if (orderNo != null && !orderNo.isBlank()) {
            orderService.markPaid(orderNo, tradeNo);
        }
        return "redirect:/orders?paySuccess=1";
    }

    /**
     * 支付宝异步通知，验签后把订单改为已支付
     */
    @PostMapping("/notify")
    @ResponseBody
    public String payNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        if (!alipayService.verifyNotify(params)) {
            return "fail";
        }
        String tradeStatus = params.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            orderService.markPaid(params.get("out_trade_no"), params.get("trade_no"));
        }
        return "success";
    }
}
