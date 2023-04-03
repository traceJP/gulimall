package com.tracejp.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/1 18:41
 */
@Data
public class CartItem {

    private Long skuId;

    private Boolean check = true;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(count.toString()));
    }

}
