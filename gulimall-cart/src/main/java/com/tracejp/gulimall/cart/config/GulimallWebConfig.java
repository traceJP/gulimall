package com.tracejp.gulimall.cart.config;

import com.tracejp.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Autowired
    private CartInterceptor cartInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(cartInterceptor).addPathPatterns("/**");
    }
}
