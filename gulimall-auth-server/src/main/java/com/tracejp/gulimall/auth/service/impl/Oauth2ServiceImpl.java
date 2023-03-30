package com.tracejp.gulimall.auth.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.tracejp.common.to.SocialUser;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.auth.config.properties.Oauth2WeiboProperties;
import com.tracejp.gulimall.auth.feign.MemberFeignService;
import com.tracejp.gulimall.auth.service.Oauth2Service;
import com.tracejp.gulimall.auth.vo.MemberResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/30 9:53
 */
@Service
@EnableConfigurationProperties(Oauth2WeiboProperties.class)
public class Oauth2ServiceImpl implements Oauth2Service {

    @Autowired
    private Oauth2WeiboProperties weiboProperties;

    @Autowired
    private MemberFeignService memberFeignService;


    @Override
    public MemberResponseVo login(String code) {

        // 使用code 换取 微博access_token 作为当前登录的验证
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("client_id", weiboProperties.getClientId());
        requestMap.put("client_secret", weiboProperties.getClientSecret());
        requestMap.put("grant_type", weiboProperties.getGrantType());
        requestMap.put("redirect_uri", weiboProperties.getRedirectUri());
        requestMap.put("code", code);
        String result = HttpUtil.post("https://api.weibo.com/oauth2/access_token", requestMap);

        // 成功 换取 access_token
        // {"access_token":"2.00xM8gwGNoQywB6d338047e1anv3bD","remind_in":"157679999","expires_in":157679999,"uid":"6363938847","isRealName":"true"}
        // 这里会直接抛出异常 说明调用失败 可以自己转换异常
        SocialUser socialUser = JSON.parseObject(result, SocialUser.class);

        // 自动注册 - 为社交账号用户绑定一个系统的会员账号
        // 1 第一次登录 关联账号
        // 2 如果社交账号已经关联了系统账号 直接登录
        R r = memberFeignService.oauthLogin(socialUser);
        if (r.getCode() != 0) {
            throw new RuntimeException("member服务远程调用失败");
        }

        return JSON.parseObject(JSON.toJSONString(r.get("data")), MemberResponseVo.class);
    }

}
