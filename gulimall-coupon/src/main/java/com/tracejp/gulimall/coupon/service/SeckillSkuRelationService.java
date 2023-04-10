package com.tracejp.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.coupon.entity.SeckillSkuRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动商品关联
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:11:24
 */
public interface SeckillSkuRelationService extends IService<SeckillSkuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SeckillSkuRelationEntity> getBySessionId(Long sessionId);
}

