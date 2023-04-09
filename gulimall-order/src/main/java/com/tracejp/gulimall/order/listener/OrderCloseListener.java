package com.tracejp.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.tracejp.gulimall.order.entity.OrderEntity;
import com.tracejp.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/9 13:07
 */
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;


    @RabbitHandler
    public void handleOrderClose(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
