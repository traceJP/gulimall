package com.tracejp.gulimall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * <p> 配置spring session的默认cookie作用域 <p/>
 * 使所有子域都可以共享session
 * 建议使用common管理spring session 总依赖，此配置文件放到common模块中
 * @author traceJP
 * @since 2023/3/30 18:04
 */
@Configuration
public class GulimallSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainName("gulimall.com");
        cookieSerializer.setCookieName("GULISESSION");
        return cookieSerializer;
    }

    /**
     * 可以重写redisSerializer中的序列化反序列化方法 自定义序列化的方式
     * 也可以直接使用Jackson写好的序列化反序列化方法直接代替RedisSerializer
     *
     * TODO BUG 注意！！！！！！！！！！！！！！！
     * springSessionDefaultRedisSerializer()方法名不能改，否则会报错
     * redis序列化拿取bean是通过RedisSerializer的实现类优先级拿取
     * 但是！！！！！
     * SpringSession中拿取序列化对象是通过@Qualifier("springSessionDefaultRedisSerializer")拿取
     * 按Bean的默认名称拿取！！！！！！！！
     * 详细直接查看该Bean的调用方使用的注解即可！
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

}
