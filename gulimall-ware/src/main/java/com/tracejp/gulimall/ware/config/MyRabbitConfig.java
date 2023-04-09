package com.tracejp.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
     * 业务要求：（库存锁定消息队列设计）
     * 交换机：stock-event-exchange
     * 队列：stock.release.stock.queue（解锁库存消费队列）、stock.delay.queue（延迟队列）
     *
     * 库存锁定：给交换机发送一条消息（路由到延迟队列中）
     * 1、如果分布式事物失败：则延迟队列会等待60000ms后将其值为死信，转发给解锁库存消费队列（参考StockLockedReleaseHandle类）
     * - 其中会通过WareOrderTask(Detail)Entity向数据库中添加锁定标志位
     * - 当锁定失败时，真正的库存记录会因当前的事物而回滚（WareSkuEntity、WareOrderTask）
     **** -- 所以这里防止的是 order服务 在调用锁定库存成功后，order服务出现问题，此时库存锁定没有会滚，但应该算锁定失败。
     * - 所以监听 stock.release.stock.queue 队列的消费者首先需要去数据库中查出是否存在WareOrderTask对应的Id。
     * -- 如果存在，则解锁，否则不用解锁
     *
     * 2、如果用户去手动取消订单（用户未支付）：则直接向交换机发送一条消息，转发给解锁库存消费队列即可
     */

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        // exclusive：是否独占队列（如果为true，则只能有一个消费者进行监听）
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public Queue stockDelayQueue() {
        // 配置延迟队列参数
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 60000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Binding stockReleaseStockBinding() {
        return new Binding(
                "stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null
        );
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding(
                "stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null
        );
    }

//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void initRabbitMQ(Message message) {
        // RabbitMQ初始化交换机、队列、绑定关系，在Spring启动时加入IOC容器中将自动创建
        // 前提：RabbitMQ使用懒加载模式，需要队列有监听者才会自动创建
        // 如果RabbitMQ服务器中存在同名队列，将不会进行覆盖
//    }

}
