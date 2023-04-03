package com.tracejp.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.tracejp.common.constant.CartConstant;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.cart.feign.ProductFeignService;
import com.tracejp.gulimall.cart.interceptor.CartInterceptor;
import com.tracejp.gulimall.cart.service.CartService;
import com.tracejp.gulimall.cart.to.SkuInfoTo;
import com.tracejp.gulimall.cart.to.UserInfoTo;
import com.tracejp.gulimall.cart.vo.Cart;
import com.tracejp.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/1 18:58
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolExecutor executor;


    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartRedisOps();
        String cartItemString = (String) cartOps.get(skuId.toString());

        CartItem cartItem;
        if (StringUtils.isEmpty(cartItemString)) {  // 添加购物车
            cartItem = new CartItem();
            cartItem.setCheck(true);
            cartItem.setCount(num);

            // getSkuInfo
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                if (skuInfo.getCode() == 0) {
                    SkuInfoTo skuInfoTo = JSON.parseObject(JSON.toJSONString(skuInfo.get("skuInfo")), SkuInfoTo.class);
                    cartItem.setSkuId(skuId);
                    cartItem.setTitle(skuInfoTo.getSkuTitle());
                    cartItem.setPrice(skuInfoTo.getPrice());
                    cartItem.setImage(skuInfoTo.getSkuDefaultImg());
                }
            }, executor);

            // getSkuSaleAttrValue
            CompletableFuture<Void> getSkuSaleAttrValueTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValuesList = productFeignService.getSkuSaleAttrValuesAsStringList(skuId);
                cartItem.setSkuAttr(skuSaleAttrValuesList);
            }, executor);

            // 需要抛出异常 不能添加空的商品到购物车 使用get
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValueTask).get();

        } else {  // 修改数量后重新添加

            cartItem = JSON.parseObject(cartItemString, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
        }

        // 添加购物车
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));

        return cartItem;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartRedisOps();
        String cartItemString = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(cartItemString)) {
            return null;
        }
        return JSON.parseObject(cartItemString, CartItem.class);
    }

    @Override
    public Cart getCart() {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();

        // 获取临时购物车
        List<CartItem> tempCartItems = getCartItems(userInfo.getUserKey());
        cart.setItems(tempCartItems);

        // 是否登录
        if (userInfo.getUserId() != null) {  // 已登录
            if (!CollectionUtils.isEmpty(tempCartItems)) {  // 存在临时购物车 合并
                tempCartItems.forEach(item -> {
                    try {
                        addToCart(item.getSkuId(), item.getCount());
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                // 删除临时购物车
                clearCart(userInfo.getUserKey());
            }

            // 查询 用户（或合并后的用户） 的购物车
            List<CartItem> newCart = getCartItems(userInfo.getUserId().toString());
            cart.setItems(newCart);
        }

        return cart;
    }

    @Override
    public void clearCart(String id) {
        String cartKey = CartConstant.CART_PREFIX + id;
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        if (cartItem != null) {
            cartItem.setCheck(check == 1);
        }
        BoundHashOperations<String, Object, Object> cartRedisOps = getCartRedisOps();
        cartRedisOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        if (cartItem != null) {
            cartItem.setCount(num);
        }
        BoundHashOperations<String, Object, Object> cartRedisOps = getCartRedisOps();
        cartRedisOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartRedisOps = getCartRedisOps();
        cartRedisOps.delete(skuId.toString());
    }

    /**
     * 获取购物车中所有的购物项 通过拼接前的id查询
     */
    private List<CartItem> getCartItems(String id) {
        String cartKey = CartConstant.CART_PREFIX + id;
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        List<Object> cartItemStringList = cartOps.values();
        List<CartItem> cartItems = null;
        if (!CollectionUtils.isEmpty(cartItemStringList)) {
            cartItems = cartItemStringList.stream().map(item -> {
                String cartItemString = (String) item;
                return JSON.parseObject(cartItemString, CartItem.class);
            }).collect(Collectors.toList());
        }

        return cartItems;
    }

    /**
     * reids 数据结构 Map<key1, Map<key2, String>>
     * key1: 用户登录的唯一标识
     * value: Map<String, String>
     * key2: skuId
     * value: CartItem
     * <p>
     * 获取购物车Redis的绑定Hash操作对象 => 绑定当前用户的购物车 (key1)
     * 绑定后会自动在 redis 中创建一个hash类型的key，通过返回的 BoundHashOperations 对象就可以操作这个hash类型的key
     * 类似返回了个Map map;映射到redis中的hash
     */
    private BoundHashOperations<String, Object, Object> getCartRedisOps() {

        UserInfoTo userInfo = CartInterceptor.threadLocal.get();

        String cartKey = CartConstant.CART_PREFIX;
        if (userInfo.getUserId() != null) {
            cartKey += userInfo.getUserId();
        } else {
            cartKey += userInfo.getUserKey();
        }

        return redisTemplate.boundHashOps(cartKey);
    }


}
