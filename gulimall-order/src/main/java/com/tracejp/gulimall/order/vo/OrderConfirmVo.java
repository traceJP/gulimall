package com.tracejp.gulimall.order.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/4 8:34
 */
@Data
public class OrderConfirmVo {

    /**
     * 当前会员的收货地址
     */
    private List<MemberAddressVo> memberAddressVos;

    /**
     * 购物车中选中的购物项
     */
    private List<OrderItemVo> items;

    /**
     * 商品是否有库存
     * 使用 map 保存 skuId，在 thymeleaf 中可以使用 confirmOrderData.stocks[item.skuId] 获取
     */
    private Map<Long, Boolean> stocks;

    /**
     * 会员积分
     * TODO：可自定义业务其他优惠卷信息
     */
    private Integer integration;

    /**
     * 订单总额
     */
    private BigDecimal total;

    /**
     * 商品总数量
     */
    private Integer count;

    /**
     * 应付总额
     */
    private BigDecimal payPrice;

    /**
     * 防重令牌
     */
    private String orderToken;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (!CollectionUtils.isEmpty(items)) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    public Integer getCount() {
        int count = 0;
        if (!CollectionUtils.isEmpty(items)) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
