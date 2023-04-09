package com.tracejp.gulimall.member.config;

import com.tracejp.gulimall.member.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/4 8:25
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }

}
