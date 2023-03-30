package com.tracejp.gulimall.auth.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/30 10:09
 */
@Data
@ConfigurationProperties(prefix = "oauth2.weibo")
public class Oauth2WeiboProperties {

    private String clientId;

    private String clientSecret;

    private String grantType;

    private String redirectUri;


}
