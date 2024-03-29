package com.tracejp.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.to.SkuHasStockTo;
import com.tracejp.common.to.mq.OrderTo;
import com.tracejp.common.to.mq.StockLockedTo;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.ware.entity.WareOrderTaskEntity;
import com.tracejp.gulimall.ware.entity.WareSkuEntity;
import com.tracejp.gulimall.ware.vo.LockStockResult;
import com.tracejp.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:16:23
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    /**
     * skuId
     * wareId
     */
    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds);

    void orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo stockLockedTo);

    void unLockStock(OrderTo orderTo);

    WareOrderTaskEntity getWareOrderTaskEntityByOrderSn(String orderSn);

}

