package com.tracejp.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/6 14:34
 */
@Data
public class FareVo {

    private BigDecimal fare;

    private MemberAddressVo address;

}
