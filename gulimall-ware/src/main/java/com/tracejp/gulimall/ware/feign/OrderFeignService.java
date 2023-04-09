package com.tracejp.gulimall.ware.feign;

import com.tracejp.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/8 21:16
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);

}
