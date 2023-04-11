package com.tracejp.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tracejp.common.to.SeckillSkuRedisTo;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.product.entity.SkuImagesEntity;
import com.tracejp.gulimall.product.entity.SpuInfoDescEntity;
import com.tracejp.gulimall.product.feign.SeckillFeignService;
import com.tracejp.gulimall.product.service.*;
import com.tracejp.gulimall.product.vo.SeckillSkuVo;
import com.tracejp.gulimall.product.vo.SkuItemSaleAttrVo;
import com.tracejp.gulimall.product.vo.SkuItemVo;
import com.tracejp.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.product.dao.SkuInfoDao;
import com.tracejp.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


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

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        LambdaQueryWrapper<SkuInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuInfoEntity::getSpuId, spuId);
        return this.list(wrapper);
    }

    @Override
    public SkuItemVo item(Long skuId) {

        SkuItemVo skuItemVo = new SkuItemVo();

        // 1. sku 基本信息获取 pms_sku_info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = this.getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, threadPoolExecutor);

        // 2. sku 图片信息获取 pms_sku_images
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);

        // 3. 获取 spu 的销售属性组合
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, threadPoolExecutor);

        // 4. 获取 spu 的介绍
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, threadPoolExecutor);

        // 5. 获取 spu 的规格参数信息
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            List<SpuItemAttrGroupVo> attrGroupVos =
                    attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        });

        // <业务修改> 6. 查询当前商品是否参与秒杀
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R skuSeckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
            if (skuSeckillInfo.getCode() == 0) {
                SeckillSkuRedisTo seckill =
                        JSON.parseObject(JSON.toJSONString(skuSeckillInfo.get("data")), SeckillSkuRedisTo.class);
                skuItemVo.setSeckillSkuVo(seckill);
            }
        }, threadPoolExecutor);


        // 等待所有任务完成
        // 异步编排
        // * allOf() 方法返回一个 CompletableFuture<Void> 对象，它在所有的 CompletableFuture 完成后完成。
        CompletableFuture.allOf(infoFuture, imagesFuture, saleAttrFuture, descFuture, baseAttrFuture, seckillFuture)
                .join();
        return skuItemVo;
    }

}