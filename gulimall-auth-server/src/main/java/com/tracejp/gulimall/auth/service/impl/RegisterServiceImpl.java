package com.tracejp.gulimall.auth.service.impl;

import com.tracejp.common.constant.AuthServerConstant;
import com.tracejp.common.to.UserRegistTo;
import com.tracejp.common.utils.R;
import com.tracejp.common.utils.RRException;
import com.tracejp.gulimall.auth.feign.MemberFeignService;
import com.tracejp.gulimall.auth.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/29 20:01
 */
@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public void register(UserRegistTo to) throws RRException {

        String redisKey = AuthServerConstant.SMS_CODE_CACHE_PREFIX + to.getPhone();

        // 校验验证码
        String redisValue = redisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(redisValue)) {

            // 验证码_时间戳
            String code = redisValue.split("_")[0];

            if (!code.equals(to.getCode())) {
                throw new RRException("验证码错误");
            }

            // 注册
            redisTemplate.delete(redisKey);
            R r = memberFeignService.regist(to);
            if (r.getCode() != 0) {
                throw new RuntimeException(r.get("msg").toString());
            }

        } else {
            // 验证码错误
            throw new RRException("验证码错误");
        }

    }

}
