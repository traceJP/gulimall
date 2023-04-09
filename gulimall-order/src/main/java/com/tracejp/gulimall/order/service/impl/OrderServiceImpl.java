package com.tracejp.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.constant.OrderConstant;
import com.tracejp.common.to.SkuHasStockTo;
import com.tracejp.common.to.mq.OrderTo;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.order.bo.OrderCreateBo;
import com.tracejp.gulimall.order.dao.OrderDao;
import com.tracejp.gulimall.order.entity.OrderEntity;
import com.tracejp.gulimall.order.entity.OrderItemEntity;
import com.tracejp.gulimall.order.enume.OrderStatusEnum;
import com.tracejp.gulimall.order.feign.CartFeignService;
import com.tracejp.gulimall.order.feign.MemberFeignService;
import com.tracejp.gulimall.order.feign.ProductFeignService;
import com.tracejp.gulimall.order.feign.WareFeignService;
import com.tracejp.gulimall.order.interceptor.LoginUserInterceptor;
import com.tracejp.gulimall.order.service.OrderItemService;
import com.tracejp.gulimall.order.service.OrderService;
import com.tracejp.gulimall.order.vo.*;
//import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private ProductFeignService productFeignService;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ThreadPoolExecutor executor;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder(Long userId) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        // 查收获地址信息
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            List<MemberAddressVo> address = memberFeignService.getAddress(userId);
            orderConfirmVo.setMemberAddressVos(address);
        }, executor);

        // 查选中购物项
        // 1 需要携带当前用户id，因为要结账，其必须登录，所以cart服务中的方法需要通过用户id进行查询
        // 2 或者将当前服务的请求头中的用户信息传递给cart服务，cart服务从请求头中获取用户信息，从而进行查询
        // 2.1 请求头信息传递，需要单独配置 feign 的拦截器，通过拦截器将每次请求发出前的请求头信息传递给下游服务
        // 2.2 注意：在异步线程中，RequestContextHolder.getRequestAttributes() 为空
        // 因为 RequestContextHolder 的本质是一个 ThreadLocal，在两个线程中是不通用的
        // 所以需要在异步线程中手动设置上主线程的 RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(items);

        }, executor).thenRunAsync(() -> {  // 查询商品库存
            List<OrderItemVo> items = orderConfirmVo.getItems();
            if (!CollectionUtils.isEmpty(items)) {
                // 查询商品库存
                List<Long> skuIds = items.parallelStream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
                R skusHasStock = wareFeignService.getSkusHasStock(skuIds);
                if (skusHasStock.getCode() == 0) {
                    List<SkuHasStockTo> data =
                            JSON.parseArray(JSON.toJSONString(skusHasStock.get("data")), SkuHasStockTo.class);
                    if (!CollectionUtils.isEmpty(data)) {
                        Map<Long, Boolean> hasStockMap = data.stream()
                                .collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                        orderConfirmVo.setStocks(hasStockMap);
                    }
                }
            }
        });


        // 查会员积分信息：TODO 需要调用远程服务查询用户请求，这里直接使用老数据代替
        Integer integration = LoginUserInterceptor.loginUser.get().getIntegration();
        orderConfirmVo.setIntegration(integration);

        // 设置防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        final int timeout = 30;
        redisTemplate.opsForValue()
                .set(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId, token, timeout, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        CompletableFuture.allOf(addressFuture, cartFuture).get();
        return orderConfirmVo;
    }

    /**
     * 使用 seata ：@GlobalTransactional 开启主事物，其他分布式服务方使用原本的 @Transactional 注解开启分布式事物即可
     * seata使用需要代理数据源，使用包装器模式将数据源进行代理，这里使用了 seata-spring-boot-starter 帮忙自动代理了数据源
     *
     * 这里使用 rabbitMQ 控制 订单和库存之间的事物 一致性 （柔性事物：可靠消息 + 最终一致性 方案）
     */
//    @GlobalTransactional
    @Override
    public OrderSubmitResponseVo submitOrder(OrderSubmitVo orderSubmitVo, Long userId) {
        OrderSubmitResponseVo responseVo = new OrderSubmitResponseVo();

        // 验证订单防重令牌
        String orderToken = orderSubmitVo.getOrderToken();
        // 通过 lua脚本 将令牌的验证和删除（两个操作）原子化，必须要两个操作同时加锁。也可以使用synchronized进行加锁（性能不好）
        // 'del', KEYS[1] 成功返回1，失败返回0 如果ARGV参数与拿到的token相同，则删除令牌，否则返回0
        // 使用redisTemplate执行lua脚本，构造RedisScript对象，指定返回值，和传递keys（list）和argv（...obj）
        final String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        List<String> scriptKeys = new ArrayList<>();
        scriptKeys.add(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId);
        Long result = redisTemplate.execute(redisScript, scriptKeys, orderToken);
        if (result == 0L) {
            // 验证失败
            responseVo.setCode(1);
            return responseVo;
        }

        // 创建订单 创建订单项
        OrderCreateBo order = createOrder(orderSubmitVo, userId);

        // 价格验证
        BigDecimal payAmount = order.getOrder().getPayAmount();
        BigDecimal oldPrice = orderSubmitVo.getPayPrice();
        if (Math.abs(payAmount.subtract(oldPrice).doubleValue()) > 0.01) {
            responseVo.setCode(2);
            return responseVo;
        }

        // 订单保存
        this.save(order.getOrder());
        orderItemService.saveBatch(order.getOrderItems());

        // 锁定库存
        WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
        wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
        if (!CollectionUtils.isEmpty(order.getOrderItems())) {
            List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map(item -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                // 只传递 skuId 和 count 和 name
                orderItemVo.setSkuId(item.getSkuId());
                orderItemVo.setCount(item.getSkuQuantity());
                orderItemVo.setTitle(item.getSkuName());
                return orderItemVo;
            }).collect(Collectors.toList());
            wareSkuLockVo.setLocks(orderItemVos);
        }
        R lockStock = wareFeignService.orderLockStock(wareSkuLockVo);
        if (lockStock.getCode() != 0) {
            responseVo.setCode(3);
            return responseVo;
        }

        // 发送订单创建消息
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());

        responseVo.setOrder(order.getOrder());
        responseVo.setCode(0);
        return responseVo;
    }

    @Override
    public OrderEntity getByOrderSn(String orderSn) {
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderEntity::getOrderSn, orderSn);
        return this.getOne(wrapper);
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {

        OrderEntity orderEntityLast = this.getById(orderEntity.getId());
        // 关单条件：订单状态为待付款状态（新建状态）
        if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
            // 关闭订单
            OrderEntity orderEntityUpdate = new OrderEntity();
            orderEntityUpdate.setId(orderEntityLast.getId());
            orderEntityUpdate.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(orderEntityUpdate);

            // TODO MQ连续的消息传递 必须保证消息一定能发出去，否则会造成消息丢失
            // 解决方案：发送者消息确认机制
            // - 使用消息发送者的确认机制，发送者发送消息后，将其在数据库中保存
            // - 每个消息都在数据库保存一份，定期扫描数据库，将没有发送成功的消息再次发送出去
            // 释放其他订单业务 - 库存解锁 & 优惠券返还
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntityLast, orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange",
                    "order.release.other", orderTo);
        }

    }

    /**
     * 使用 bo和其他方法 分离业务 方便观察事物
     * 抽取方法：创建订单-》
     *              构建订单记录
     *              创建订单项 -》
     *                  构建订单项
     */
    private OrderCreateBo createOrder(OrderSubmitVo orderSubmitVo, Long userId) {

        String orderSn = IdWorker.getTimeId();  // mybatis-plus提供的时间uuid生成工具类

        // Build Order
        OrderEntity orderEntity = buildOrderEntity(orderSubmitVo, userId, orderSn);

        // Build order item
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);

        // 通过 orderItems 为 orderEntity 计算真实总价
        OrderEntity orderEntityForPrice = computePrice(orderEntity, orderItems);
        orderEntity.setTotalAmount(orderEntityForPrice.getTotalAmount());
        orderEntity.setCouponAmount(orderEntityForPrice.getCouponAmount());
        orderEntity.setIntegrationAmount(orderEntityForPrice.getIntegrationAmount());
        orderEntity.setPromotionAmount(orderEntityForPrice.getPromotionAmount());
        orderEntity.setPayAmount(orderEntityForPrice.getPayAmount());
        orderEntity.setIntegration(orderEntityForPrice.getIntegration());
        orderEntity.setGrowth(orderEntityForPrice.getGrowth());

        OrderCreateBo orderCreateBo = new OrderCreateBo();
        orderCreateBo.setOrder(orderEntity);
        orderCreateBo.setOrderItems(orderItems);
        return orderCreateBo;
    }

    private OrderEntity computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems) {

        // 累加变量
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;

        for (OrderItemEntity orderItem : orderItems) {
            total = total.add(orderItem.getRealAmount());
            coupon = coupon.add(orderItem.getCouponAmount());
            integration = integration.add(orderItem.getIntegrationAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            giftIntegration += orderItem.getGiftIntegration();
            giftGrowth += orderItem.getGiftGrowth();
        }

        orderEntity.setTotalAmount(total);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));  // 订单总额 + 运费
        orderEntity.setIntegration(giftIntegration);
        orderEntity.setGrowth(giftGrowth);

        return orderEntity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        List<OrderItemEntity> orderItems = null;
        if (!CollectionUtils.isEmpty(cartItems)) {
            orderItems = cartItems.stream()
                    .map(item -> buildOrderItem(orderSn, item))
                    .collect(Collectors.toList());
        }
        return orderItems;
    }

    private OrderItemEntity buildOrderItem(String orderSn, OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        // 订单信息
        orderItemEntity.setOrderSn(orderSn);

        // 商品 sku 信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());
        String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);  // 使用 ; 分割 sku属性

        // 商品 spu 信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(item.getSkuId());
        if (spuInfo.getCode() == 0) {
            SpuInfoVo spuInfoVo = JSON.parseObject(JSON.toJSONString(spuInfo.get("data")), SpuInfoVo.class);
            orderItemEntity.setSpuId(spuInfoVo.getId());
            orderItemEntity.setSpuName(spuInfoVo.getSpuName());
            orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
            orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        }

        // TODO 优惠信息

        // 积分信息 ：购买的商品数量 * 单个商品的积分（价格） = 总积分
        orderItemEntity.setGiftGrowth(item.getPrice().intValue() * item.getCount());
        orderItemEntity.setGiftIntegration(item.getPrice().intValue() * item.getCount());

        // 订单项的价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0.0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0.0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.0"));

        // 订单实际价格
        BigDecimal realAmount = item.getTotalPrice().subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realAmount);

        return orderItemEntity;
    }

    private OrderEntity buildOrderEntity(OrderSubmitVo orderSubmitVo, Long userId, String orderSn) {
        OrderEntity orderEntity = new OrderEntity();

        // 订单基本信息
        orderEntity.setOrderSn(orderSn);
        orderEntity.setCreateTime(new Date());
        orderEntity.setMemberId(userId);
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setDeleteStatus(0);
        final Integer autoConfirmDay = 7;
        orderEntity.setAutoConfirmDay(autoConfirmDay);

        // 运费信息
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        if (fare.getCode() == 0) {
            FareVo fareVo = JSON.parseObject(JSON.toJSONString(fare.get("data")), FareVo.class);

            // 运费
            orderEntity.setFreightAmount(fareVo.getFare());

            // 地址信息
            MemberAddressVo addressVo = fareVo.getAddress();
            orderEntity.setReceiverCity(addressVo.getCity());
            orderEntity.setReceiverDetailAddress(addressVo.getDetailAddress());
            orderEntity.setReceiverName(addressVo.getName());
            orderEntity.setReceiverPhone(addressVo.getPhone());
            orderEntity.setReceiverPostCode(addressVo.getPostCode());
            orderEntity.setReceiverProvince(addressVo.getProvince());
            orderEntity.setReceiverRegion(addressVo.getRegion());
        }
        return orderEntity;
    }


}