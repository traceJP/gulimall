package com.tracejp.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/16 20:19
 */
@Configuration
public class GulimallElasticSearchConfig {

    @Value("${elasticsearch.host}")
    private String host = "localhost";

    @Value("${elasticsearch.port}")
    private Integer port = 9200;

    @Value("${elasticsearch.scheme}")
    private String scheme = "http";

    /**
     * 配置es客户端
     * @return RestHighLevelClient
     */
    @Bean
    public RestHighLevelClient elasticSearchRestHighLevelClient() {
        HttpHost httpHost = new HttpHost(host, port, scheme);
        RestClientBuilder builder = RestClient.builder(httpHost);
        return new RestHighLevelClient(builder);
    }

}
