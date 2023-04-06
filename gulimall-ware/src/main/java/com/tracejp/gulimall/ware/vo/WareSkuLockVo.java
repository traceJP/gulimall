package com.tracejp.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/6 20:05
 */
@Data
public class WareSkuLockVo {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 需要锁定的所有库存信息
     */
    List<OrderItemVo> locks;

}
