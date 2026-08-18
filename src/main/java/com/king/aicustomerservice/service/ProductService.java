package com.king.aicustomerservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.king.aicustomerservice.entity.Category;
import com.king.aicustomerservice.entity.Product;
import com.king.aicustomerservice.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务类
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    /**
     * 用户端分页查询已上架商品
     */
    public Page<Product> pageForUser(int page, int size, Long categoryId, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .like(StringUtils.hasText(keyword), Product::getName, keyword)
                .orderByDesc(Product::getId);
        return fillCategoryName(productMapper.selectPage(new Page<>(page, size), wrapper));
    }

    /**
     * 管理端分页查询全部商品
     */
    public Page<Product> pageForAdmin(int page, int size, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .like(StringUtils.hasText(keyword), Product::getName, keyword)
                .orderByDesc(Product::getId);
        return fillCategoryName(productMapper.selectPage(new Page<>(page, size), wrapper));
    }

    /**
     * 查询已上架商品列表（首页使用）
     */
    public List<Product> listOnSale(Long categoryId, String keyword) {
        return pageForUser(1, 50, categoryId, keyword).getRecords();
    }

    /**
     * 根据ID查询商品
     */
    public Product findById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        Category category = categoryService.findById(product.getCategoryId());
        if (category != null) {
            product.setCategoryName(category.getName());
        }
        return product;
    }

    /**
     * 新增商品，名称不可与已有商品重复
     */
    public void save(Product product) {
        assertUniqueName(product);
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }
        productMapper.insert(product);
    }

    /**
     * 更新商品，名称不可与其他商品重复
     */
    public void update(Product product) {
        assertUniqueName(product);
        productMapper.updateById(product);
    }

    /**
     * 删除商品
     */
    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    /**
     * 统计商品数量
     */
    public long count() {
        return productMapper.selectCount(null);
    }

    /**
     * 扣减库存，库存不足则失败
     */
    public void decreaseStock(Long productId, int quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new RuntimeException("商品【" + product.getName() + "】库存不足");
        }
        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);
    }

    /**
     * 填充分类名称，方便前端展示
     */
    private Page<Product> fillCategoryName(Page<Product> page) {
        List<Category> categories = categoryService.listAllIncludeDisabled();
        Map<Long, String> nameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
        page.getRecords().forEach(item -> item.setCategoryName(nameMap.get(item.getCategoryId())));
        return page;
    }

    /**
     * 同一商品名称不可重复（忽略首尾空格）
     */
    private void assertUniqueName(Product product) {
        String name = product.getName() == null ? "" : product.getName().trim().replaceAll("\\s+", " ");
        if (!StringUtils.hasText(name)) {
            throw new RuntimeException("请填写商品名称");
        }
        product.setName(name);
        Long exists = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getName, name)
                .ne(product.getId() != null, Product::getId, product.getId()));
        if (exists != null && exists > 0) {
            throw new RuntimeException("商品名称已存在，请勿重复添加");
        }
    }
}
