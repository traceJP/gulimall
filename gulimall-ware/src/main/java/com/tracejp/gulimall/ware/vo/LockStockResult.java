package com.tracejp.gulimall.ware.vo;

import lombok.Data;

/**
 * <p> to <p/>
 *
 * @author traceJP
 * @since 2023/4/6 20:08
 */
@Data
public class LockStockResult {

    private Long skuId;

    private Integer num;

    /**
     * 是否被锁定
     */
    private Boolean locked;

}
