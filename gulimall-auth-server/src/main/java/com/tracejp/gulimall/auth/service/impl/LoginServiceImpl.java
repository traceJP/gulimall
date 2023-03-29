package com.tracejp.gulimall.auth.service.impl;

import com.tracejp.common.constant.AuthServerConstant;
import com.tracejp.common.exception.BizCodeEnum;
import com.tracejp.common.to.UserLoginTo;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.auth.feign.MemberFeignService;
import com.tracejp.gulimall.auth.feign.ThirdPartFeignService;
import com.tracejp.gulimall.auth.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/29 19:17
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public R sendCode(String phone) {

        String redisKey = AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone;

        String redisCode = redisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(redisCode)) {
            long oldTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - oldTime < 60 * 1000) {
                // 60秒内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // String code = UUID.randomUUID().toString().substring(0, 5);
        // 生成5位随机整数字符串   redisValue 拼串 <code>_<时间戳>   这里可以封装一个code和时间戳的bo类，转json保存
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 10000));
        String redisSaveCode = code + "_" + System.currentTimeMillis();
        // redis 保存拼串   sms:code:<手机号>_时间戳
        redisTemplate.opsForValue().set(redisKey, redisSaveCode, 10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }

    @Override
    public R login(UserLoginTo to) {

        return memberFeignService.login(to);
    }

}
