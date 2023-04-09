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
        // TODO BUG 其他业务不使用 totalPrice属性时 会因为 Feign 调用时 total 或 count 字段为 null 导致空指针异常
        if (this.price == null || this.count == null) {
            return new BigDecimal("0");
        }
        return this.price.multiply(new BigDecimal(count.toString()));
    }

}
