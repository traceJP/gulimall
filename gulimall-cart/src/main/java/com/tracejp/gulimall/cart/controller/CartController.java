package com.tracejp.gulimall.cart.controller;

import com.tracejp.gulimall.cart.service.CartService;
import com.tracejp.gulimall.cart.vo.Cart;
import com.tracejp.gulimall.cart.vo.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/1 18:57
 */
@Slf4j
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("checked") Integer checked) {
        cartService.checkItem(skuId, checked);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItem(skuId, num);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 用户如果登录：使用目标用户uuid做购物车的key
     * 用户未登录：用户第一次使用购物车功能，都会给一个临时用户身份 uuid
     * 浏览器以 cookie 的形式保存
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) {
        Cart cart = cartService.getCart();

        model.addAttribute("cart", cart);
        return "cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(skuId, num);
        } catch (Exception e) {
            log.error("购物车添加失败{}", e.toString());
        }

        // 会自动在 url 后面 拼接上 skuId
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 解决页面重复提交问题：用户重新刷新会再次提交添加购物车逻辑
     * 采用两个访问路径，当 addToCart执行完毕后，重定向到 addToCartSuccess 路径地址
     * 然后在 addToCartSuccess 路径地址中获取 skuId，然后再次查询购物车数据
     * 此时页面再此刷新，执行的则是 addToCartSuccess 路径地址，而不是 addToCart 路径地址
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItem cartItem = cartService.getCartItem(skuId);

        model.addAttribute("cartItem", cartItem);
        return "success";
    }

}
