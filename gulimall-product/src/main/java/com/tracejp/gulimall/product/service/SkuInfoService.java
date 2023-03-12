package com.tracejp.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku信息
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 19:11:23
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * - params
     * key
     * catelogId
     * brandId
     * min
     * max
     */
    PageUtils queryPageByCondition(Map<String, Object> params);
}

