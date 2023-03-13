package com.tracejp.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:16:23
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    /**
     * key
     * status
     * wareId
     */
    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

