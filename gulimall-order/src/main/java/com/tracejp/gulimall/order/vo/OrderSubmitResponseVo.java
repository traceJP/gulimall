package com.tracejp.gulimall.order.vo;

import com.tracejp.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/6 15:27
 */
@Data
public class OrderSubmitResponseVo {

    /**
     * 订单信息：状态码为0时有值
     */
    private OrderEntity order;

    /**
     * 状态码： 0成功 1失败
     */
    private Integer code;


}
