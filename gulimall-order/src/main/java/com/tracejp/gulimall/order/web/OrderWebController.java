package com.tracejp.gulimall.order.web;

import com.tracejp.common.vo.MemberResponseVo;
import com.tracejp.gulimall.order.interceptor.LoginUserInterceptor;
import com.tracejp.gulimall.order.service.OrderService;
import com.tracejp.gulimall.order.vo.OrderConfirmVo;
import com.tracejp.gulimall.order.vo.OrderSubmitResponseVo;
import com.tracejp.gulimall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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


    /**
     * 提交订单
     * 使用 RabbitMQ 作为事物回滚的中间件
     * 这里不使用 Seata ，使用Seata控制全局事物 需要加锁 严重影响高并发
     * 所以希望使用 RabbitMQ ，将失败的订单进行消息传递，然后进行回滚
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        OrderSubmitResponseVo responseVo = orderService.submitOrder(orderSubmitVo, memberResponseVo.getId());
        String msg = "";
        switch (responseVo.getCode()) {
            // 下单成功
            case 0:
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            // 下单失败
            case 1:
                msg = "订单已过期";
                break;
            case 2:
                msg = "订单信息错误";
                break;
            case 3:
                msg = "库存锁定失败";
                break;
            default:
                msg = "未知错误";
                break;
        }
        redirectAttributes.addFlashAttribute("msg", msg);
        return "redirect:http://order.gulimall.com/toTrade";
    }

}
