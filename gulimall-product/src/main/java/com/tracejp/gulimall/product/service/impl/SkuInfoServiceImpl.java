package com.tracejp.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.product.dao.SkuInfoDao;
import com.tracejp.gulimall.product.entity.SkuInfoEntity;
import com.tracejp.gulimall.product.service.SkuInfoService;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        LambdaQueryWrapper<SkuInfoEntity> wrapper = new LambdaQueryWrapper<SkuInfoEntity>();

        /**
         * - params
         * key
         * catelogId
         * brandId
         * min
         * max
         */
        String queryKey = (String) params.get("key");
        if (!StringUtils.isEmpty(queryKey)) {
            wrapper.and(w -> w.eq(SkuInfoEntity::getSkuId, queryKey).or().like(SkuInfoEntity::getSkuName, queryKey));
        }

        // String -> Long
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !catelogId.equals("0")) {
            wrapper.eq(SkuInfoEntity::getCatalogId, catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !brandId.equals("0")) {
            wrapper.eq(SkuInfoEntity::getBrandId, brandId);
        }

        BigDecimal min = new BigDecimal((String) params.get("min"));
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge(SkuInfoEntity::getPrice, min);
        }

        BigDecimal max = new BigDecimal((String) params.get("max"));
        if (!StringUtils.isEmpty(max) && max.compareTo(new BigDecimal("0")) == 1) {
            wrapper.le(SkuInfoEntity::getPrice, max);
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

}