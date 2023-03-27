package com.tracejp.gulimall.thirdparty.config;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.tracejp.gulimall.thirdparty.config.properties.SmsConfigProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 22:10
 */
@Configuration
public class SmsConfig {

    @Autowired
    private SmsConfigProperties smsConfigProperties;

    @Bean
    public AsyncClient smsClient() {
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(smsConfigProperties.getAccessKey())
                .accessKeySecret(smsConfigProperties.getSecretKey())
                .build());

        return AsyncClient.builder()
                .region(smsConfigProperties.getRegion())
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride(smsConfigProperties.getEndpoint())
                )
                .build();
    }

}
