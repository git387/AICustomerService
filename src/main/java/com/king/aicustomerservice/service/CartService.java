package com.king.aicustomerservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.king.aicustomerservice.entity.CartItem;
import com.king.aicustomerservice.entity.Product;
import com.king.aicustomerservice.mapper.CartItemMapper;
import com.king.aicustomerservice.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务类
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    /**
     * 查询当前用户购物车，并填充商品信息
     */
    public List<CartItem> listByUser(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
        for (CartItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
                item.setProductImage(product.getImage());
                item.setPrice(product.getPrice());
                item.setStock(product.getStock());
            }
        }
        return items;
    }

    /**
     * 加入购物车，已存在则累加数量
     */
    public void add(Long userId, Long productId, int quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new RuntimeException("商品不存在或已下架");
        }
        CartItem exist = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));
        if (exist == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            cartItemMapper.insert(item);
        } else {
            exist.setQuantity(exist.getQuantity() + quantity);
            cartItemMapper.updateById(exist);
        }
    }

    /**
     * 修改购物车数量
     */
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        if (quantity < 1) {
            throw new RuntimeException("数量至少为 1");
        }
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new RuntimeException("购物车项不存在");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    /**
     * 删除购物车项
     */
    public void delete(Long userId, Long cartItemId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item != null && item.getUserId().equals(userId)) {
            cartItemMapper.deleteById(cartItemId);
        }
    }

    /**
     * 清空用户购物车（下单成功后调用）
     */
    public void clear(Long userId) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }
}
