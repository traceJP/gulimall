package com.tracejp.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tracejp.common.to.SkuHasStockTo;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.ware.entity.WareInfoEntity;
import com.tracejp.gulimall.ware.feign.ProductFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.ware.dao.WareSkuDao;
import com.tracejp.gulimall.ware.entity.WareSkuEntity;
import com.tracejp.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq(WareSkuEntity::getSkuId, skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq(WareSkuEntity::getWareId, wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WareSkuEntity::getSkuId, skuId).eq(WareSkuEntity::getWareId, wareId);
        List<WareSkuEntity> entities = this.list(wrapper);
        if (CollectionUtils.isEmpty(entities)) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);

            // 查询冗余信息
            R skuInfo = productFeignService.getSkuInfo(skuId);
            String skuName = (String) skuInfo.get("skuName");
            wareSkuEntity.setSkuName(skuName);

            this.save(wareSkuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockTo> getSkusHasStock(@RequestBody List<Long> skuIds) {

        // TODO 此处循环查库 优化
        // SELECT * FROM wms_ware_sku WHERE sku_id in ({skuIds}) GROUP BY sku_id
        // 判断 stock - stock_locked 封装 to 返回

        return skuIds.stream().map(sku -> {
            SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
            // 查询当前sku的总库存量
            Long skuStock = this.baseMapper.getSkuStock(sku);
            skuHasStockTo.setSkuId(sku);
            skuHasStockTo.setHasStock(skuStock != null && skuStock > 0);

            return skuHasStockTo;
        }).collect(Collectors.toList());
    }
}