package com.tracejp.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/6 16:41
 */
@Data
public class FareVo {

    private BigDecimal fare;

    private MemberAddressVo address;

}
