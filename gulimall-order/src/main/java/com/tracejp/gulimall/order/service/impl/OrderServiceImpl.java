package com.tracejp.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.tracejp.common.to.SkuHasStockTo;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.order.feign.CartFeignService;
import com.tracejp.gulimall.order.feign.MemberFeignService;
import com.tracejp.gulimall.order.feign.WareFeignService;
import com.tracejp.gulimall.order.interceptor.LoginUserInterceptor;
import com.tracejp.gulimall.order.vo.MemberAddressVo;
import com.tracejp.gulimall.order.vo.OrderConfirmVo;
import com.tracejp.gulimall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.order.dao.OrderDao;
import com.tracejp.gulimall.order.entity.OrderEntity;
import com.tracejp.gulimall.order.service.OrderService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WareFeignService wareFeignService;

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

        CompletableFuture.allOf(addressFuture, cartFuture).get();
        return orderConfirmVo;
    }

}