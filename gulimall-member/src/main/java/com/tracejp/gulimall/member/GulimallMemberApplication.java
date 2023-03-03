package com.tracejp.gulimall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

// 扫描所有的feign客户端接口
@EnableFeignClients(basePackages = "com.tracejp.gulimall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.tracejp.gulimall.member.dao")
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
