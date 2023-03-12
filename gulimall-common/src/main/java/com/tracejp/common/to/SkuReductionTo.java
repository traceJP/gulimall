package com.tracejp.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/12 15:51
 */
@Data
public class SkuReductionTo {

    private Long skuId;

    private int fullCount;
    private BigDecimal discount;
    private int countStatus;

    private BigDecimal fullPrice;
    private BigDecimal reducePrice;

    private int priceStatus;

    private List<MemberPrice> memberPrice;

}
