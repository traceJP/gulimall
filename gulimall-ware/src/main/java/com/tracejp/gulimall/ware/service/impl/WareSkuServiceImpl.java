package com.tracejp.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tracejp.common.to.SkuHasStockTo;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.ware.bo.SkuWareHasStock;
import com.tracejp.gulimall.ware.exception.NoStockException;
import com.tracejp.gulimall.ware.feign.ProductFeignService;
import com.tracejp.gulimall.ware.vo.LockStockResult;
import com.tracejp.gulimall.ware.vo.OrderItemVo;
import com.tracejp.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public void orderLockStock(WareSkuLockVo vo) {

        // 锁定库存：
        // 业务要求：1个失败 全部失败
        // 注意：一个商品在多个仓库中都有库存，需要分别查出库存
        List<OrderItemVo> locks = vo.getLocks();
        if (!CollectionUtils.isEmpty(locks)) {
            // 找到所有包含商品的仓库，只要有存在商品的仓库即可
            locks.stream().map(item -> {
                SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
                skuWareHasStock.setSkuId(item.getSkuId());
                skuWareHasStock.setNum(item.getCount());
                // SELECT ware_id FROM `wms_ware_sku` WHERE sku_id = getSkuId() AND stock-stock_locked > 1
                List<Long> wareIds = this.baseMapper.listWareIdHasSkuStock(item.getSkuId());
                skuWareHasStock.setWareIds(wareIds);
                return skuWareHasStock;

            }).forEach(item -> {
                // 尝试扣减库存
                Long skuId = item.getSkuId();
                List<Long> wareIds = item.getWareIds();
                if (CollectionUtils.isEmpty(wareIds)) {
                    throw new NoStockException(skuId);
                }
                // 当前商品锁定的标志位，如果有仓库被锁定，则返回true
                boolean skuStockedFlag = false;
                for (Long wareId : wareIds) {
                    // stock-stock_locked > getCount() ：当前库存 - 已经锁定的库存 = 剩余库存 > 当前准备锁定的库存
                    // SELECT ware_id FROM `wms_ware_sku` WHERE sku_id = getSkuId() AND ware_id = getWareId() AND stock-stock_locked > getCount()
                    Long fail = this.baseMapper.lockSkuStock(skuId, wareId, item.getNum());
                    if (fail == 1) {
                        skuStockedFlag = true;
                        break;
                    }
                }
                // 均无仓库的商品可以被锁定，为false
                if (!skuStockedFlag) {
                    throw new NoStockException(skuId);
                }
            });
        }

    }
}