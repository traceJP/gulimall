package com.tracejp.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/11 18:39
 */
@Data
public class SeckillOrderTo {

    private String orderSn;

    private Long promotionSessionId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private Integer num;

    private Long memberId;

}
