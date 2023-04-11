package com.tracejp.gulimall.seckill.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/3 18:55
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Bean
    public MessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    /**
     * 业务要求：
     * 商品秒杀成功，发送消息（路由键order.seckill.order）到交换机order-event-exchange，路由到order.seckill.order.queue队列
     * 由订单服务监听即可
     */

}
