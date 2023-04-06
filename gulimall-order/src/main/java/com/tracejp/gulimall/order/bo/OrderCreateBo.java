package com.tracejp.gulimall.order.bo;

import com.tracejp.gulimall.order.entity.OrderEntity;
import com.tracejp.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/6 16:32
 */
@Data
public class OrderCreateBo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    private BigDecimal fare;

}
