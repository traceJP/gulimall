package com.tracejp.gulimall.auth.controller;

import com.tracejp.common.utils.R;
import com.tracejp.gulimall.auth.feign.ThirdPartFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 22:35
 */
@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;


    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // String code = UUID.randomUUID().toString().substring(0, 5);
        // 生成5位随机整数字符串
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 10000));
        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }

}
