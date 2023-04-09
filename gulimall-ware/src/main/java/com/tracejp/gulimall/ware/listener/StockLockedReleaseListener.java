package com.tracejp.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.tracejp.common.to.mq.StockLockedTo;
import com.tracejp.gulimall.ware.service.WareSkuService;
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
 * @since 2023/4/8 20:51
 */
@Service
// @RabbitListener 作用在类上，该对象下方法均可不用指定queues属性对应值，适用于重载
@RabbitListener(queues = "stock.release.stock.queue")
public class StockLockedReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 自动解锁库存方法（监听库存释放消息队列：业务要求参考MyRabbitConfig）
     * <p>
     * 自动解锁逻辑： 需要判断库存是因为Order服务的失败导致的分布式事物回滚，还是因为本身库存锁定失败导致的回滚
     * 查询是否存在 wareOrderTaskDetailEntity
     * 四种情况
     * 1、Order服务失败，没有订单：存在，因为Ware锁定库存本身执行无任何问题 => 解锁
     * 2、Order服务成功，存在订单：存在，即一切下单业务均正常，只是因为锁定库存的时候强制发送了一条消息，延迟队列消息到期了，需要进行处理，所以不解锁。
     * 3、Order服务成功，存在订单：存在，即一切下单业务均正常，但是用户提前关闭了订单（取消了订单），此时订单的状态位为 4 ，所以应该解锁库存
     * 4、自身锁定失败：不存在，因为自身锁定局部事物导致了wareOrderTaskDetailEntity跟随回滚了 => 库存信息也回滚了 => 无需解锁
     * <p>
     * 方法改造：将 RabbitMQ签收逻辑 和 库存解锁逻辑 分离
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {

        // 这里只要正常解锁库存，就签收消息
        // 否则抛出异常，就拒签
        try {
            wareSkuService.unLockStock(stockLockedTo);
            // 签收消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 拒签
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

}
