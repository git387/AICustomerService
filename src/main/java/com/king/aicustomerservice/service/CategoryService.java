package com.king.aicustomerservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.king.aicustomerservice.entity.Category;
import com.king.aicustomerservice.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务类
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 查询所有启用的分类
     */
    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSortOrder));
    }

    /**
     * 查询全部分类（含禁用），供管理端和下拉框使用
     */
    public List<Category> listAllIncludeDisabled() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
    }

    /**
     * 分页查询分类
     */
    public Page<Category> pageList(int page, int size) {
        return categoryMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
    }

    /**
     * 根据ID查询
     */
    public Category findById(Long id) {
        return categoryMapper.selectById(id);
    }

    /**
     * 新增分类
     */
    public void save(Category category) {
        categoryMapper.insert(category);
    }

    /**
     * 更新分类
     */
    public void update(Category category) {
        categoryMapper.updateById(category);
    }

    /**
     * 删除分类
     */
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
