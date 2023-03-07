package com.tracejp.gulimall.product.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/7 21:16
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.tracejp.gulimall.product.dao")
public class MyBatisConfig {

    // 导入 Mybatis Plus 分页插件 目前 MP 版本为 2.x 注意参考 MP2.x文档
    @Bean
    public PaginationInterceptor mybatisPlusInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();

        paginationInterceptor.setOverflow(true);

        paginationInterceptor.setLimit(1000);

        return paginationInterceptor;
    }

}
