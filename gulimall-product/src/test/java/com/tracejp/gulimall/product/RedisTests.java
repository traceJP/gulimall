package com.tracejp.gulimall.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/19 21:38
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    public void test() {
        stringRedisTemplate.opsForValue().set("hello", "world");
        log.info("保存成功");
    }


}
