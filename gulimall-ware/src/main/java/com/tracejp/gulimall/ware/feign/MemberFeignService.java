package com.tracejp.gulimall.ware.feign;

import com.tracejp.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/6 14:25
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R getAddrInfo(@PathVariable("id") Long id);

}
