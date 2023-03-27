package com.tracejp.gulimall.thirdparty;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 20:33
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSms {

    @Test
    public void testSms() throws ExecutionException, InterruptedException {

        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId("xxxxxxxxxxxxxxx")
                .accessKeySecret("xxxxxxxxxxxxxxx")
                .build());

        AsyncClient client = AsyncClient.builder()
                .region("cn-hangzhou")
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dysmsapi.aliyuncs.com")
                )
                .build();

        SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                .phoneNumbers("17607960216")
                .signName("阿里云短信测试")
                .templateCode("SMS_154950909")
                .templateParam("{\"code\":\"12345\"}")
                .build();

        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);

        SendSmsResponse resp = response.get();
        System.out.println(new Gson().toJson(resp));

        client.close();

    }

}
