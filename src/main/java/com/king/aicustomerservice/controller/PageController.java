package com.king.aicustomerservice.controller;

import com.king.aicustomerservice.entity.SysUser;
import com.king.aicustomerservice.service.CategoryService;
import com.king.aicustomerservice.service.OrderService;
import com.king.aicustomerservice.service.ProductService;
import com.king.aicustomerservice.service.SecurityUserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户端 Thymeleaf 页面路由
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final SecurityUserHelper securityUserHelper;

    /**
     * 向所有页面注入当前登录用户
     */
    @ModelAttribute("currentUser")
    public SysUser currentUser() {
        return securityUserHelper.currentUserOrNull();
    }

    /**
     * 商城首页
     */
    @GetMapping("/")
    public String index(@RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) String keyword,
                        Model model) {
        model.addAttribute("categories", categoryService.listAll());
        model.addAttribute("products", productService.listOnSale(categoryId, keyword));
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("keyword", keyword);
        return "index";
    }

    /**
     * 商品详情页
     */
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "product-detail";
    }

    /**
     * 购物车页
     */
    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    /**
     * 结算页
     */
    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

    /**
     * 我的订单
     */
    @GetMapping("/orders")
    public String orders(Model model) {
        SysUser user = securityUserHelper.requireUser();
        model.addAttribute("orders", orderService.listByUser(user.getId()));
        return "orders";
    }

    /**
     * 订单详情（仅本人订单）
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        SysUser user = securityUserHelper.requireUser();
        model.addAttribute("order", orderService.findOwned(id, user.getId()));
        return "order-detail";
    }

    /**
     * 收货地址管理页
     */
    @GetMapping("/addresses")
    public String addresses() {
        return "addresses";
    }

    /**
     * 智能客服页
     */
    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    /**
     * 登录页
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 注册页
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }
}
