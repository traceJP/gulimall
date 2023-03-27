package com.tracejp.gulimall.thirdparty.controller;

import com.tracejp.common.utils.R;
import com.tracejp.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 22:20
 */
@RestController
@RequestMapping("thirdparty/sms")
public class SmsController {

    @Autowired
    private SmsComponent smsComponent;


    // TODO 这里测试验证码只接收4-6位纯数字
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        try {
            smsComponent.sendSmsCode(phone, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok();
    }

}
