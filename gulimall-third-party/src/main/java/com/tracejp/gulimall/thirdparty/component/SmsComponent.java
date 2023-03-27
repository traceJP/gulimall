package com.tracejp.gulimall.thirdparty.component;

import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.google.gson.Gson;
import com.tracejp.gulimall.thirdparty.config.properties.SmsConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 20:51
 */
@Slf4j
@Component
public class SmsComponent {

    @Autowired
    private SmsConfigProperties smsConfigProperties;

    @Autowired
    private AsyncClient smsAsyncClient;


    public void sendSmsCode(String phone, String code) throws ExecutionException, InterruptedException {

        SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                .phoneNumbers(phone)
                .signName(smsConfigProperties.getSignName())
                .templateCode(smsConfigProperties.getTemplateCode())
                // {"code":"1234"}
                .templateParam("{\"code\":\"" + code + "\"}")
                .build();

        CompletableFuture<SendSmsResponse> response = smsAsyncClient.sendSms(sendSmsRequest);

        SendSmsResponse resp = response.get();
        log.info("已发送短信" + new Gson().toJson(resp));
    }

}
