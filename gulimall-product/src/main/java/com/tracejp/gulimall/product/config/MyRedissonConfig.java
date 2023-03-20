package com.tracejp.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p> Redisson整合配置 <p/>
 *
 * @author traceJP
 * @since 2023/3/20 14:12
 */
@Configuration
public class MyRedissonConfig {

    @Value("${spring.redis.host}")
    private String redisHost = "localhost";

    @Value("${spring.redis.port}")
    private String redisPort = "6379";


    @Bean
    public RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }

}
