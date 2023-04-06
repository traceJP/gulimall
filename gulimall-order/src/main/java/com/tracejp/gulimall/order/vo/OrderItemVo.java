package com.tracejp.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/4 8:41
 */
@Data
public class OrderItemVo {

    private Long skuId;

    private Boolean check = true;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    // 库存状态
//    private Boolean hasStock = true;
    private BigDecimal weight;

    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(count.toString()));
    }

}
