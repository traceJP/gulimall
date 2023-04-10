package com.tracejp.gulimall.seckill.feign;

import com.tracejp.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/10 14:45
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/latest3DaysSession")
    R getLatest3DaysSession();

}
