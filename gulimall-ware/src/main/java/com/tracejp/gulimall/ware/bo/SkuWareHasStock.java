package com.tracejp.gulimall.ware.bo;

import lombok.Data;

import java.util.List;

/**
 * <p> 1对多 的经典业务封装字段 <p/>
 *
 * @author traceJP
 * @since 2023/4/6 20:28
 */
@Data
public class SkuWareHasStock {

    private Long skuId;

    private List<Long> wareIds;

    private Integer num;

}
