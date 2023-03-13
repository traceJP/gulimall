package com.tracejp.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.ware.entity.PurchaseEntity;
import com.tracejp.gulimall.ware.vo.MergeVo;
import com.tracejp.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:16:23
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivedList(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void receivedPurchase(List<Long> ids);

    void finishPurchase(PurchaseDoneVo purchaseDoneVo);
}

