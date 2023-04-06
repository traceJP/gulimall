package com.tracejp.gulimall.order.feign;

import com.tracejp.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/4 9:04
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/addresses/{memberId}")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);

}
