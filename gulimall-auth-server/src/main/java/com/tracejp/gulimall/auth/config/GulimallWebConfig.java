package com.tracejp.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p> 重写 WebMvcConfigurer 的 addViewControllers方法 <p/>
 * 功能：可以代替 @RequestMapping 中 只做页面跳转的功能
 * @author traceJP
 * @since 2023/3/27 20:03
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // registry.addViewController("/login.html").setViewName("login");

        registry.addViewController("/reg.html").setViewName("reg");

    }

}
