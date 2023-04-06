package com.tracejp.gulimall.order.web;

import com.tracejp.common.vo.MemberResponseVo;
import com.tracejp.gulimall.order.interceptor.LoginUserInterceptor;
import com.tracejp.gulimall.order.service.OrderService;
import com.tracejp.gulimall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.ExecutionException;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/4 8:24
 */
@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;


    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        OrderConfirmVo confirmVo = orderService.confirmOrder(memberResponseVo.getId());

        model.addAttribute("confirmOrderData", confirmVo);
        return "confirm";
    }

}
