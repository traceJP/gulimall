package com.tracejp.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/10 19:38
 */
@Data
public class SeckillSkuRedisTo {

    /**
     * id
     */
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;


    /**
     * sku的详细信息
     */
    private SeckillSkuTo skuInfo;

    /**
     * 当前商品秒杀的开始时间
     */
    private Long startTime;

    /**
     * 当前商品秒杀的结束时间
     */
    private Long endTime;

    /**
     * 商品秒杀随机码 - 只有当商品秒杀开始时才会暴露该随机码，用户带上该随机码才能请求秒杀接口
     * 防止脚本提前进行请求
     */
    private String randomCode;

}
