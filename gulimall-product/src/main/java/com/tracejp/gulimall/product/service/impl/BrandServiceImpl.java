package com.tracejp.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tracejp.gulimall.product.dao.CategoryBrandRelationDao;
import com.tracejp.gulimall.product.entity.AttrGroupEntity;
import com.tracejp.gulimall.product.entity.CategoryBrandRelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.product.dao.BrandDao;
import com.tracejp.gulimall.product.entity.BrandEntity;
import com.tracejp.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationDao relationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        // 处理模糊查询
        String queryKey = (String) params.get("key");
        LambdaQueryWrapper<BrandEntity> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(queryKey)) {
            wrapper.eq(BrandEntity::getBrandId, queryKey)
                    .or()
                    .like(BrandEntity::getName, queryKey);
        }

        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {

        this.updateById(brand);

        // 级联更新其他冗余表
        if (!StringUtils.isEmpty(brand.getName())) {
            CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
            categoryBrandRelationEntity.setBrandName(brand.getName());
            LambdaQueryWrapper<CategoryBrandRelationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CategoryBrandRelationEntity::getBrandId, brand.getBrandId());
            relationDao.update(categoryBrandRelationEntity, wrapper);
        }

        // TODO 更新其他关联表

    }

    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {
        return (List<BrandEntity>) this.listByIds(brandIds);
    }

}