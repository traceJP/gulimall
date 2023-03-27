package com.tracejp.gulimall.product.vo;

import lombok.Data;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 17:36
 */
@Data
public class AttrValueWithSkuIdVo {

    private String attrValue;

    // 属性所对应的所有 skuId
    private String skuIds;

}
