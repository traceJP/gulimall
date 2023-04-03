package com.tracejp.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

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
     * 自定义 RabbitTemplate 的消息确认机制 默认回调方法   参考配置文件
     */
    @PostConstruct  // @PostConstruct 用于指定spring容器内方法在spring中的生命周期：MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {
        /*
         * RabbitTemplate.ConfirmCallback() 用于指定消息发送方的确认回调  配置confirms：客户端消息是否发抵达了交换机确认。
         *
         * confirm重写方法参数：
         * correlationData：消息的唯一id
         * ack：消息是否成功收到
         * cause：失败的原因
         */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {

            }
        });

        /*
         * RabbitTemplate.ReturnCallback()  配置publisher-returns：交换机消息是否抵达了队列确认。
         * ***（只有消息发送失败才回调此方法！）
         *
         * return重写方法参数：
         * message：消息体
         * replyCode：回复的状态码（失败状态码）
         * replyText：回复的文本内容（失败状态码对应的原因）
         * exchange：当前消息使用的交换机
         * routingKey：当前消息使用的路由键
         */
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

            }
        });

    }



}
