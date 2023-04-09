package com.tracejp.gulimall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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
     *
     * 交换机：order-event-exchange
     * 队列：
     * order.delay.queue（延迟队列）
     * order.release.order.queue（订单取消、失败：释放订单服务）（订单释放完毕后需要通知stock.release.stock.queue库存服务，主动释放库存）
     * order.release.coupon.queue（订单取消、失败：优惠券返还）
     * order.finsh.user.queue（订单支付完成：用户加积分）
     * order.finsh.ware.queue（订单支付完成：库存检查、拆单）
     * stock.release.stock.queue（库存服务：释放库存）
     *
     * 订单服务创建订单成功后，发送消息到延迟队列中，等待30分钟后，如果没有收到支付成功的消息，则自动取消订单
     * 路由流程：
     * 1、创建订单，向order.delay.queue发送消息
     * 2、延迟队列中的消息过期后（或主动释放订单），会发送消息路由到 order.release.order.queue，进行释放订单操作
     * 3、释放订单操作又会发送消息路由（order.release.order.#）到其他订单相关的业务
     * 3.1、如 order.release.coupon.queue，进行优惠券返还操作
     * 3.2、如 stock.release.stock.queue，进行库存解锁操作
     * 4、订单完成后（订单支付成功），会发送消息路由（order.finish.#）到其他完成订单相关业务
     * 4.1、如 order.finish.user.queue，进行用户积分操作
     * 4.2、如 order.finish.ware.queue，进行库存检查、拆单操作
     *
     */

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Queue orderReleaseCouponQueue() {
        return new Queue("order.release.coupon.queue", true, false, false);
    }

    @Bean
    public Queue orderFinishUserQueue() {
        return new Queue("order.finish.user.queue", true, false, false);
    }

    @Bean
    public Queue orderFinshWareQueue() {
        return new Queue("order.finish.ware.queue", true, false, false);
    }

    @Bean
    public Binding createOrderBinding() {
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    @Bean
    public Binding orderReleaseCouponBinding() {
        return new Binding("order.release.coupon.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }

    @Bean
    public Binding orderReleaseWareBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }

    @Bean
    public Binding orderFinishUserBinding() {
        return new Binding("order.finish.user.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.finish.#",
                null);
    }

    @Bean
    public Binding orderFinishWareBinding() {
        return new Binding("order.finish.ware.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.finish.#",
                null);
    }



/*
    /**
     * 自定义 RabbitTemplate 的消息确认机制 默认回调方法   参考配置文件
     *//*
    @PostConstruct  // @PostConstruct 用于指定spring容器内方法在spring中的生命周期：MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {
        *//*
         * RabbitTemplate.ConfirmCallback() 用于指定消息发送方的确认回调  配置confirms：客户端消息是否发抵达了交换机确认。
         *
         * confirm重写方法参数：
         * correlationData：消息的唯一id
         * ack：消息是否成功收到
         * cause：失败的原因
         *//*
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {

            }
        });

        *//*
         * RabbitTemplate.ReturnCallback()  配置publisher-returns：交换机消息是否抵达了队列确认。
         * ***（只有消息发送失败才回调此方法！）
         *
         * return重写方法参数：
         * message：消息体
         * replyCode：回复的状态码（失败状态码）
         * replyText：回复的文本内容（失败状态码对应的原因）
         * exchange：当前消息使用的交换机
         * routingKey：当前消息使用的路由键
         *//*
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

            }
        });

    }
*/



}
