package com.tracejp.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.product.entity.SpuInfoEntity;
import com.tracejp.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 19:11:22
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    /**
     * - params
     * status
     * key
     * brandId
     * catelogId
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    void spuUp(Long spuId);
}

