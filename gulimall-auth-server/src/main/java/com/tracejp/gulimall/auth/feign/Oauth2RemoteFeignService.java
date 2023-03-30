package com.tracejp.gulimall.auth.feign;

import feign.Feign;
import feign.Param;
import feign.RequestLine;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/30 9:57
 */
public interface Oauth2RemoteFeignService {

    // TODO 使用 feign 发送远程请求的时候（其他主机） 会被 springSecurity 拦截，导致请求失败 返回 401
    // 需要配置白名单
    @Deprecated
    @RequestLine("POST /oauth2/access_token?client_id={client_id}&client_secret={client_secret}&grant_type=authorization_code&redirect_uri={redirect_uri}&code={code}")
    String getWeiboAssessToken(@Param("client_id") String clientId,
                             @Param("client_secret") String clientSecret,
                             @Param("redirect_uri") String redirectUri,
                             @Param("code") String code);

    @Deprecated
    static String sendToWeibo(String clientId, String clientSecret, String redirectUri, String code) {
        Oauth2RemoteFeignService api = Feign.builder()
                .target(Oauth2RemoteFeignService.class, "https://api.weibo.com");
        return api.getWeiboAssessToken(clientId, clientSecret, redirectUri, code);
    }

}
