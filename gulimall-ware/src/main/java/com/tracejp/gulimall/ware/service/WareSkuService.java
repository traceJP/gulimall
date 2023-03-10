package com.tracejp.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * εεεΊε­
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
}

