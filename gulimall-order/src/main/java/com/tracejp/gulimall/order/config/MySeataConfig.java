package com.tracejp.gulimall.order.config;

import org.springframework.context.annotation.Configuration;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/7 21:08
 */
@Configuration
public class MySeataConfig {

    /**
     * 当前 seata版本为 com.alibaba.cloud:spring-cloud-alibaba-seata:2.1.0.RELEASE
     * 需要自己手动包装数据源 和 配置 group name
     *
     * seata的两个依赖版本
     * 1、com.alibaba.cloud:spring-cloud-alibaba-seata:2.1.0.RELEASE
     * - 需要自己！！手动包装数据源！！ \ !!!并且注意：需要在resources文件夹下引入 seata的配置文件：file.conf 和 registry.conf
     * - 在 registry配置文件中配置 nacos的注册地址，以及其附属配置文件file.conf中配置持久化
     * - 并且需要在每个项目中都进行引入和配置！！！
     * 其中需要修改的配置项：alibaba.seata.tx-service-group: ${spring.application.name}-fescar-service-group
     * * 或者在file.conf中修改：service.vgroup_mapping.${spring.application.name}-fescar-service-group=default
     *
     * ----------------------------------------------------------------------------------------------------------------
     *
     * 2、com.alibaba.cloud:spring-cloud-alibaba-seata:2.2.9.RELEASE （最新版本）
     * - 废弃了配置项：alibaba.seata.tx-service-group: ${spring.application.name}-fescar-service-group
     * - 废弃了配置文件：file.conf 和 registry.conf
     * - 直接在application.properties中配置 seata的配置项即可，如在其中配置 nacos的注册地址等
     * - 实例：（参考ruoyi-seata的配置）
     *# seata配置
     * seata:
     *   enabled: true
     *   # Seata 应用编号，默认为 ${spring.application.name}
     *   application-id: ${spring.application.name}
     *   # Seata 事务组编号，用于 TC 集群名
     *   tx-service-group: ${spring.application.name}-group
     *   # 关闭自动代理
     *   enable-auto-data-source-proxy: false
     *   # 服务配置项
     *   service:
     *     # 虚拟组和分组的映射
     *     vgroup-mapping:
     *       ruoyi-system-group: default
     *     # 分组和 Seata 服务的映射
     *     grouplist:
     *       default: 127.0.0.1:8091
     *   config:
     *     type: file
     *   registry:
     *     type: file
     *
     */
/*    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        if (StringUtils.hasText(dataSourceProperties.getName())) {
            dataSource.setPoolName(dataSourceProperties.getName());
        }
        return new DataSourceProxy(dataSource);
    }*/

}
