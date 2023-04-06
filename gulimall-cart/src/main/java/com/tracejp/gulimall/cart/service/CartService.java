package com.tracejp.gulimall.cart.service;

import com.tracejp.gulimall.cart.vo.Cart;
import com.tracejp.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/1 18:58
 */
public interface CartService {

    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getCurrentUserCartItems();

}
