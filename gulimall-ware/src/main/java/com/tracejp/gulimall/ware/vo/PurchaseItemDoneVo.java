package com.tracejp.gulimall.ware.vo;

import lombok.Data;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/13 19:58
 */
@Data
public class PurchaseItemDoneVo {

    private Long itemId;

    private Integer status;

    private String reason;

}
