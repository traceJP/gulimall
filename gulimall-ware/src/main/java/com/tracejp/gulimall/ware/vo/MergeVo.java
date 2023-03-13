package com.tracejp.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/13 19:08
 */
@Data
public class MergeVo {

    private Long purchaseId;

    private List<Long> items;

}
