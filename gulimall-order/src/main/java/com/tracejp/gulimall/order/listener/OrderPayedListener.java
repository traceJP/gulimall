package com.tracejp.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.tracejp.gulimall.order.service.OrderService;
import com.tracejp.gulimall.order.utils.AlipayTemplate;
import com.tracejp.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/9 20:40
 */
@RestController
public class OrderPayedListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayTemplate alipayTemplate;


    /**
     * TODO 未连接到公网 未测试
     * 支付宝支付成功后的异步回调接口（在alipay中配置notify-url即可指定接口）（！需要公网可访问！）
     * 请求：当用户在支付宝支付页进行支付确认付款后，支付宝会向此接口发送请求
     * 响应：当支付宝收到我们的响应数据（字符串 success）后就不会再通知了
     * 具体可以参考支付宝的文档（支付宝将持续向此接口发送通知直到你响应 success字符串数据）
     */
    @PostMapping("/payed/notify")
    public String handleAliPayed(PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException {

        // 验签：防止其他人伪造参数调用此方法
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipayPublicKey(),
                alipayTemplate.getCharset(), alipayTemplate.getSignType()); //调用SDK验证签名

        if (signVerified) {
            return orderService.handlePayResult(payAsyncVo);
        }

        return "error";
    }

}
