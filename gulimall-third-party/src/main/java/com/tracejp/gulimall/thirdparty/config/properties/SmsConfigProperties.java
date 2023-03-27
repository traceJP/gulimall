package com.tracejp.gulimall.thirdparty.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 20:52
 */
@Data
@Configuration
public class SmsConfigProperties {

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessKey;

    @Value("${spring.cloud.alicloud.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.alicloud.sms.region}")
    private String region;

    @Value("${spring.cloud.alicloud.sms.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.sms.sign-name}")
    private String signName;

    @Value("${spring.cloud.alicloud.sms.template-code}")
    private String templateCode;

}
