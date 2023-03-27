package com.tracejp.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 15:13
 */
@Data
public class SpuItemAttrGroupVo {

    private String groupName;

    private List<Attr> attrs;

}
