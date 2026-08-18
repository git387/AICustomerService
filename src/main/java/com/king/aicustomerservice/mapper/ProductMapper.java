package com.king.aicustomerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.king.aicustomerservice.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper 接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
