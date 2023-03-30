package com.tracejp.gulimall.auth.controller;

import com.tracejp.common.constant.AuthServerConstant;
import com.tracejp.gulimall.auth.service.Oauth2Service;
import com.tracejp.common.vo.MemberResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/30 9:51
 */
@Controller
public class Oauth2Controller {

    @Autowired
    private Oauth2Service oauth2Service;


    @GetMapping("/oauth2/weibo/success")
    // 微博回调携带code参数
    public String weibo(@RequestParam("code") String code, HttpSession session) {
        try {
            MemberResponseVo memberEntity = oauth2Service.login(code);
            session.setAttribute(AuthServerConstant.LOGIN_USER, memberEntity);
        } catch (Exception e) {
            return "redirect:http://auth.gulimall.com/login.html";
        }

        return "redirect:http://gulimall.com";
    }

}
