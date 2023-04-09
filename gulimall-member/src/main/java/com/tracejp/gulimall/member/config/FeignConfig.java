package com.tracejp.gulimall.member.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/4 20:16
 */
@Configuration
public class FeignConfig {

    /**
     * 使用 RequestInterceptor 拦截器将原请求的请求头信息同步到新请求中
     * 该拦截器将会在 Feign 请求发送前，执行请求对象的构造时调用
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                template.header("Cookie", request.getHeader("Cookie"));
            }
        };
    }

}
