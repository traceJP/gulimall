package com.tracejp.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.order.entity.OrderEntity;
import com.tracejp.gulimall.order.vo.OrderConfirmVo;
import com.tracejp.gulimall.order.vo.OrderSubmitResponseVo;
import com.tracejp.gulimall.order.vo.OrderSubmitVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:15:10
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder(Long userId) throws ExecutionException, InterruptedException;

    OrderSubmitResponseVo submitOrder(OrderSubmitVo orderSubmitVo, Long userId);

    OrderEntity getByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);

}

