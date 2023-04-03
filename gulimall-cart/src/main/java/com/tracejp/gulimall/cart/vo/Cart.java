package com.tracejp.gulimall.cart.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/1 18:41
 */
@Data
public class Cart {

    private List<CartItem> items;

    private Integer countNum;

    private Integer countType;

    private BigDecimal totalAmount;

    private BigDecimal reduce = new BigDecimal("0.00");

    public Integer getCountNum() {
        int count = 0;
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        return CollectionUtils.isEmpty(items) ? 0 : items.size();
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = new BigDecimal("0");
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItem item : items) {
                if (item.getCheck()) {
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            }
        }
        return totalAmount.subtract(getReduce());
    }

}
