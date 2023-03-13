package com.tracejp.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/13 19:57
 */
@Data
public class PurchaseDoneVo {

    private Long id;

    private List<PurchaseItemDoneVo> items;

}
