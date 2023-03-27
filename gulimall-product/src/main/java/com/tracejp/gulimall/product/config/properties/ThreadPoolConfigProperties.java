package com.tracejp.gulimall.product.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 18:14
 */
// @ConfigurationProperties 只需要使用EnableConfigurationProperties开启即可
// 不需要使用 @Component 注解加入容器
@Data
@ConfigurationProperties(prefix = "gulimall.thread")
public class ThreadPoolConfigProperties {

    private Integer coreSize;

    private Integer maxSize;

    private Integer keepAliveTime;

}
