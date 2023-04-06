package com.tracejp.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/6 15:14
 */
@Data
public class OrderSubmitVo {

    /**
     * 收货地址
     */
    private Long addrId;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 防重令牌
     */
    private String orderToken;

    /**
     * 支付页面应付价格
     */
    private BigDecimal payPrice;

    /*
     * 无需提交需要购买的商品，去购物车再获取一遍即可
     * 提交其他数据 如优惠、发票等
     */

}
