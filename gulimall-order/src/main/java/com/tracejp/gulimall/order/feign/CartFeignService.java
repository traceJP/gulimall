package com.tracejp.gulimall.order.feign;

import com.tracejp.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/4 9:16
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    // 可以接收用户id，避免feign调用过程中忽略掉的请求头信息
    List<OrderItemVo> getCurrentUserCartItems();

}
