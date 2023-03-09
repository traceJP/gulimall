package com.tracejp.gulimall.product.service.impl;

import com.tracejp.gulimall.product.dao.BrandDao;
import com.tracejp.gulimall.product.dao.CategoryDao;
import com.tracejp.gulimall.product.entity.BrandEntity;
import com.tracejp.gulimall.product.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.product.dao.CategoryBrandRelationDao;
import com.tracejp.gulimall.product.entity.CategoryBrandRelationEntity;
import com.tracejp.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private CategoryDao categoryDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {

        BrandEntity brandEntity = brandDao.selectById(categoryBrandRelation.getBrandId());
        categoryBrandRelation.setBrandName(brandEntity.getName());

        CategoryEntity categoryEntity = categoryDao.selectById(categoryBrandRelation.getCatelogId());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelation);

    }

}