package com.tracejp.gulimall.thirdparty.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/4 19:33
 */
@Configuration
@Data
public class OssConfig {

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessKey;

    @Value("${spring.cloud.alicloud.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.oss.bucket-name}")
    private String bucketName;

    public String getHost() {
        return "https://" + bucketName + "." + endpoint;
    }

}
