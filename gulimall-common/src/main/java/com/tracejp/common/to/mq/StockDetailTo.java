package com.tracejp.common.to.mq;

import lombok.Data;

/**
 * <p> WareOrderTaskDetailEntity <p/>
 *
 * @author traceJP
 * @since 2023/4/8 20:44
 */
@Data
public class StockDetailTo {

    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    private Long wareId;

    private Integer lockStatus;

}
