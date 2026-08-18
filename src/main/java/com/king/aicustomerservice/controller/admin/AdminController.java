package com.king.aicustomerservice.controller.admin;

import com.king.aicustomerservice.common.Result;
import com.king.aicustomerservice.entity.Category;
import com.king.aicustomerservice.entity.Order;
import com.king.aicustomerservice.entity.Product;
import com.king.aicustomerservice.entity.SysUser;
import com.king.aicustomerservice.entity.UserAddress;
import com.king.aicustomerservice.enums.OrderStatus;
import com.king.aicustomerservice.service.AddressService;
import com.king.aicustomerservice.service.CategoryService;
import com.king.aicustomerservice.service.DashboardService;
import com.king.aicustomerservice.service.FileUploadService;
import com.king.aicustomerservice.service.KnowledgeService;
import com.king.aicustomerservice.service.OrderService;
import com.king.aicustomerservice.service.ProductService;
import com.king.aicustomerservice.service.SysUserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 管理员后台接口
 * 仪表盘、商品、分类、订单、用户和知识库管理
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final SysUserService sysUserService;
    private final KnowledgeService knowledgeService;
    private final FileUploadService fileUploadService;
    private final AddressService addressService;

    /**
     * 仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(dashboardService.overview());
    }

    /**
     * 商品分页
     */
    @GetMapping("/products")
    public Result<?> products(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword) {
        return Result.ok(productService.pageForAdmin(page, size, keyword));
    }

    /**
     * 新增商品
     */
    @PostMapping("/products")
    public Result<Void> saveProduct(@RequestBody Product product) {
        productService.save(product);
        return Result.ok("保存成功", null);
    }

    /**
     * 更新商品
     */
    @PutMapping("/products/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.update(product);
        return Result.ok("更新成功", null);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok("删除成功", null);
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/upload/product-image")
    public Result<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.saveProductImage(file);
        return Result.ok(Map.of("url", url));
    }

    /**
     * 分类分页
     */
    @GetMapping("/categories")
    public Result<?> categories(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(categoryService.pageList(page, size));
    }

    /**
     * 全部分类（下拉框）
     */
    @GetMapping("/categories/all")
    public Result<?> allCategories() {
        return Result.ok(categoryService.listAllIncludeDisabled());
    }

    /**
     * 新增分类
     */
    @PostMapping("/categories")
    public Result<Void> saveCategory(@RequestBody Category category) {
        categoryService.save(category);
        return Result.ok("保存成功", null);
    }

    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.update(category);
        return Result.ok("更新成功", null);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok("删除成功", null);
    }

    /**
     * 订单分页
     */
    @GetMapping("/orders")
    public Result<?> orders(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String keyword) {
        return Result.ok(orderService.pageForAdmin(page, size, status, keyword));
    }

    /**
     * 订单详情（含商品明细）
     */
    @GetMapping("/orders/{id}")
    public Result<Order> orderDetail(@PathVariable Long id) {
        return Result.ok(orderService.findByIdForAdmin(id));
    }

    /**
     * 修改订单状态
     */
    @PutMapping("/orders/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        orderService.updateStatus(id, request.getStatus());
        return Result.ok("状态已更新", null);
    }

    /**
     * 用户分页
     */
    @GetMapping("/users")
    public Result<?> users(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String keyword) {
        return Result.ok(sysUserService.pageList(page, size, keyword));
    }

    /**
     * 更新用户（禁用/启用、改昵称等）
     */
    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        user.setPassword(null);
        sysUserService.update(user);
        return Result.ok("更新成功", null);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.ok("删除成功", null);
    }

    /**
     * 收货地址分页
     */
    @GetMapping("/addresses")
    public Result<?> addresses(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) Long userId,
                               @RequestParam(required = false) String keyword) {
        return Result.ok(addressService.pageForAdmin(page, size, userId, keyword));
    }

    /**
     * 管理员新增收货地址
     */
    @PostMapping("/addresses")
    public Result<UserAddress> saveAddress(@RequestBody UserAddress address) {
        return Result.ok("保存成功", addressService.saveByAdmin(address));
    }

    /**
     * 管理员更新收货地址
     */
    @PutMapping("/addresses/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody UserAddress address) {
        address.setId(id);
        addressService.updateByAdmin(address);
        return Result.ok("更新成功", null);
    }

    /**
     * 管理员删除收货地址
     */
    @DeleteMapping("/addresses/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteByAdmin(id);
        return Result.ok("删除成功", null);
    }

    /**
     * 知识库文件列表
     */
    @GetMapping("/knowledge")
    public Result<?> knowledge() {
        return Result.ok(knowledgeService.listAll());
    }

    /**
     * 上传知识库并向量化
     */
    @PostMapping("/knowledge")
    public Result<?> uploadKnowledge(@RequestParam("file") MultipartFile file) {
        return Result.ok("上传并向量化成功", knowledgeService.uploadAndEmbed(file));
    }

    /**
     * 删除知识库文件
     */
    @DeleteMapping("/knowledge/{id}")
    public Result<Void> deleteKnowledge(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.ok("删除成功", null);
    }

    /**
     * 订单状态请求体
     */
    @Data
    public static class StatusRequest {
        private OrderStatus status;
    }
}
