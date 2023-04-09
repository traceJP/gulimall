package com.tracejp.common.to.mq;

import lombok.Data;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/8 20:43
 */
@Data
public class StockLockedTo {

    /**
     * 库存工作单id - WareOrderTask
     */
    private Long id;

    /**
     * WareOrderTaskDetail
     */
    private StockDetailTo detail;

}
