package com.tracejp.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.product.dao.ProductAttrValueDao;
import com.tracejp.gulimall.product.entity.ProductAttrValueEntity;
import com.tracejp.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<ProductAttrValueEntity> baseListForSpu(Long spuId) {
        LambdaQueryWrapper<ProductAttrValueEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductAttrValueEntity::getSpuId, spuId);
        return this.list(wrapper);
    }

    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        // 直接删除 防止有属性覆盖
        LambdaQueryWrapper<ProductAttrValueEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductAttrValueEntity::getSpuId, spuId);
        this.remove(wrapper);

        List<ProductAttrValueEntity> collect = entities.stream().map(item -> {
            item.setSpuId(spuId);
            return item;
        }).collect(Collectors.toList());
        this.saveBatch(collect);

    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        LambdaQueryWrapper<ProductAttrValueEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductAttrValueEntity::getSpuId, spuId);
        return this.list(wrapper);
    }

}