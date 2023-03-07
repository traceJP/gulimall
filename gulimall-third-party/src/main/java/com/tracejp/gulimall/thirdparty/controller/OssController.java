package com.tracejp.gulimall.thirdparty.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.thirdparty.config.OssConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/4 19:26
 */
@RestController
@RequestMapping("thirdparty/oss")
public class OssController {


    // 通过 寻找 OssAutoConfiguration 相关Java文件的方法，找到 SpringBoot OSS 自动装配的源码
    // 即可了解到 自动装配 返回 OSS 对象
    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;


    @GetMapping("/policy")
    public R doGet() {

        // 格式化一个年月日的日期字符串
        String formatDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir = formatDate + "/";

        Map<String, String> respMap = null;
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            // build response
            respMap = new LinkedHashMap<>();
            respMap.put("accessId", ossConfig.getAccessKey());    // AccessId = AccessKey
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", ossConfig.getHost());
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));

        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        }

        return R.ok().put("data", respMap);

    }



}
