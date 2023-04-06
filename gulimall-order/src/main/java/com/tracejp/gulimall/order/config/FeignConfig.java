package com.tracejp.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
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
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 使用 RequestContextHolder 可以拿到当前线程的请求信息
                // RequestContextHolder类是spring框架包装的servlet的工具类 =》 用于获取当前线程的请求信息
                // RequestAttributes.getRequestAttributes() 返回 RequestAttributes 转换为 Servlet 子类拿到请求头等信息。
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    HttpServletRequest request = requestAttributes.getRequest();
                    // 将原请求的请求头信息同步到新请求中
                    template.header("Cookie", request.getHeader("Cookie"));
                }
            }
        };
    }

}
