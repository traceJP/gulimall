package com.tracejp.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tracejp.common.to.SkuHasStockTo;
import com.tracejp.common.to.mq.OrderTo;
import com.tracejp.common.to.mq.StockDetailTo;
import com.tracejp.common.to.mq.StockLockedTo;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.ware.bo.SkuWareHasStock;
import com.tracejp.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.tracejp.gulimall.ware.entity.WareOrderTaskEntity;
import com.tracejp.gulimall.ware.exception.NoStockException;
import com.tracejp.gulimall.ware.feign.OrderFeignService;
import com.tracejp.gulimall.ware.feign.ProductFeignService;
import com.tracejp.gulimall.ware.service.WareOrderTaskDetailService;
import com.tracejp.gulimall.ware.service.WareOrderTaskService;
import com.tracejp.gulimall.ware.vo.LockStockResult;
import com.tracejp.gulimall.ware.vo.OrderItemVo;
import com.tracejp.gulimall.ware.vo.OrderVo;
import com.tracejp.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.ware.dao.WareSkuDao;
import com.tracejp.gulimall.ware.entity.WareSkuEntity;
import com.tracejp.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq(WareSkuEntity::getSkuId, skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq(WareSkuEntity::getWareId, wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WareSkuEntity::getSkuId, skuId).eq(WareSkuEntity::getWareId, wareId);
        List<WareSkuEntity> entities = this.list(wrapper);
        if (CollectionUtils.isEmpty(entities)) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);

            // 查询冗余信息
            R skuInfo = productFeignService.getSkuInfo(skuId);
            String skuName = (String) skuInfo.get("skuName");
            wareSkuEntity.setSkuName(skuName);

            this.save(wareSkuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockTo> getSkusHasStock(@RequestBody List<Long> skuIds) {

        // TODO 此处循环查库 优化
        // SELECT * FROM wms_ware_sku WHERE sku_id in ({skuIds}) GROUP BY sku_id
        // 判断 stock - stock_locked 封装 to 返回

        return skuIds.stream().map(sku -> {
            SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
            // 查询当前sku的总库存量
            Long skuStock = this.baseMapper.getSkuStock(sku);
            skuHasStockTo.setSkuId(sku);
            skuHasStockTo.setHasStock(skuStock != null && skuStock > 0);

            return skuHasStockTo;
        }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void orderLockStock(WareSkuLockVo vo) {

        // 保存库存工作单：用于判断库存分布式事物（追溯）
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 锁定库存：
        // 业务要求：1个失败 全部失败
        // 注意：一个商品在多个仓库中都有库存，需要分别查出库存
        List<OrderItemVo> locks = vo.getLocks();
        if (!CollectionUtils.isEmpty(locks)) {
            // 找到所有包含商品的仓库，只要有存在商品的仓库即可
            locks.stream().map(item -> {
                SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
                skuWareHasStock.setSkuId(item.getSkuId());
                skuWareHasStock.setNum(item.getCount());
                // SELECT ware_id FROM `wms_ware_sku` WHERE sku_id = getSkuId() AND stock-stock_locked > 1
                List<Long> wareIds = this.baseMapper.listWareIdHasSkuStock(item.getSkuId());
                skuWareHasStock.setWareIds(wareIds);
                return skuWareHasStock;

            }).forEach(item -> {
                // 尝试扣减库存
                Long skuId = item.getSkuId();
                List<Long> wareIds = item.getWareIds();
                if (CollectionUtils.isEmpty(wareIds)) {
                    throw new NoStockException(skuId);
                }
                // 当前商品锁定的标志位，如果有仓库被锁定，则返回true
                boolean skuStockedFlag = false;
                for (Long wareId : wareIds) {
                    // stock-stock_locked > getCount() ：当前库存 - 已经锁定的库存 = 剩余库存 > 当前准备锁定的库存
                    // SELECT ware_id FROM `wms_ware_sku` WHERE sku_id = getSkuId() AND ware_id = getWareId() AND stock-stock_locked > getCount()
                    Long fail = this.baseMapper.lockSkuStock(skuId, wareId, item.getNum());
                    if (fail == 1) {
                        skuStockedFlag = true;

                        // 库存锁定成功：发送消息至RabbitMQ：延迟队列（一个sku实体发送一条消息，逐个锁定）
                        // 需要把整个sku工作单都发出去（使用工作单id定位库存锁定，其他定位库存锁定的详细信息（锁定个数等））
                        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                        wareOrderTaskDetailEntity.setSkuId(skuId);
                        wareOrderTaskDetailEntity.setWareId(wareId);
                        wareOrderTaskDetailEntity.setTaskId(wareOrderTaskEntity.getId());
                        wareOrderTaskDetailEntity.setLockStatus(1);
                        wareOrderTaskDetailEntity.setSkuNum(item.getNum());

                        // 工作单保存 + 发送到消息队列
                        wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                        // 发送消息（entity -> to）发送
                        StockLockedTo stockLockedTo = new StockLockedTo();
                        StockDetailTo stockDetailTo = new StockDetailTo();
                        BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                        stockLockedTo.setId(wareOrderTaskEntity.getId());
                        stockLockedTo.setDetail(stockDetailTo);
                        rabbitTemplate.convertSendAndReceive("stock-event-exchange", "stock.locked", stockLockedTo);

                        break;
                    }
                }
                // 均无仓库的商品可以被锁定，为false，整个订单锁定均失败
                if (!skuStockedFlag) {
                    throw new NoStockException(skuId);
                }
            });
        }

    }

    @Override
    public void unLockStock(StockLockedTo stockLockedTo) {

        StockDetailTo stockDetailTo = stockLockedTo.getDetail();
        Long detailTaskId = stockDetailTo.getId();

        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = wareOrderTaskDetailService.getById(detailTaskId);
        if (wareOrderTaskDetailEntity != null) {  // 存在：情况1 & 情况2 & 情况3
            // 判断：通过远程方法调用 Order服务 查询
            // 保证可靠性，如果远程查询出现错误，直接将该条消息拒收即可，这样不会导致反向调用造成的消息丢失
            // 注意 try 抓取 feign调用异常 也需要拒签消息
            Long wareOrderTaskId = stockLockedTo.getId();
            WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(wareOrderTaskId);
            String orderSn = wareOrderTaskEntity.getOrderSn();

            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            if (orderStatus.getCode() == 0) {
                OrderVo orderVo = JSON.parseObject(JSON.toJSONString(orderStatus.get("data")), OrderVo.class);
                // 情况1 & 情况3
                if (orderVo == null || orderVo.getStatus() == 4) {  // 参考 order服务OrderStatusEnum
                    // 解锁：解锁前的判断条件 - 仓库工作单的状态（已锁定 1）、订单状态（已取消 4）
                    if (wareOrderTaskDetailEntity.getLockStatus() == 1) {
                        this.unLockStock(stockDetailTo.getSkuId(),
                                stockDetailTo.getWareId(), stockDetailTo.getSkuNum(), detailTaskId);
                    }
                }
            } else {
                throw new RuntimeException("远程服务调用失败");
            }

        }

    }

    @Transactional
    @Override
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity wareOrderTaskEntity = getWareOrderTaskEntityByOrderSn(orderSn);
        Long wareOrderTaskId = wareOrderTaskEntity.getId();
        // 找到所有工作单Detail项
        List<WareOrderTaskDetailEntity> taskDetails = wareOrderTaskDetailService.getListByTaskId(wareOrderTaskId);
        if (!CollectionUtils.isEmpty(taskDetails)) {
            // 解锁库存：需要添加局部事物，防止解锁项和mq消息不一致
            for (WareOrderTaskDetailEntity taskDetail : taskDetails) {
                this.unLockStock(taskDetail.getSkuId(), taskDetail.getWareId(), taskDetail.getSkuNum(), taskDetail.getId());
            }
        }
    }

    public WareOrderTaskEntity getWareOrderTaskEntityByOrderSn(String orderSn) {
        LambdaQueryWrapper<WareOrderTaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WareOrderTaskEntity::getOrderSn, orderSn);
        return wareOrderTaskService.getById(wrapper);
    }

    @Transactional
    public void unLockStock(Long skuId, Long wareId, Integer skuNum, Long detailTaskId) {
        // 解锁库存
        this.baseMapper.unLockStock(skuId, wareId, skuNum);

        // 修改状态（WareOrderTaskDetailEntity）
        WareOrderTaskDetailEntity update = new WareOrderTaskDetailEntity();
        update.setId(detailTaskId);
        update.setLockStatus(2);
        wareOrderTaskDetailService.updateById(update);
    }

}