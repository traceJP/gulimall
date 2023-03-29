package com.tracejp.gulimall.auth.feign;

import com.tracejp.common.to.UserLoginTo;
import com.tracejp.common.to.UserRegistTo;
import com.tracejp.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/29 20:29
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistTo userRegistTo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginTo to);

}
