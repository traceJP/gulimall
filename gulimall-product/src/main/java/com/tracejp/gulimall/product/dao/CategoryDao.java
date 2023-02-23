package com.tracejp.gulimall.product.dao;

import com.tracejp.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 19:11:23
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
