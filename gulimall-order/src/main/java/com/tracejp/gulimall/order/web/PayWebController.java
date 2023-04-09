package com.tracejp.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.tracejp.gulimall.order.service.OrderService;
import com.tracejp.gulimall.order.utils.AlipayTemplate;
import com.tracejp.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/9 15:17
 */
@Controller
public class PayWebController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayTemplate alipayTemplate;


    /**
     * 跳转使用支付宝支付页面
     * produces = "text/html"：返回的是一个页面
     */
    @GetMapping(value = "/aliPayOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);

        // 返回一个 form表单页面：在前后端分离应用中直接交给前端页面使用JS打开即可
        return alipayTemplate.pay(payVo);
    }

}
