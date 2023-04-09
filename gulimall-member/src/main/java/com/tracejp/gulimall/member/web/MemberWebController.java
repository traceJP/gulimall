package com.tracejp.gulimall.member.web;

import com.tracejp.common.utils.R;
import com.tracejp.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/9 16:07
 */
@Controller
public class MemberWebController {

    @Autowired
    private OrderFeignService orderFeignService;


    @GetMapping("/memberOrder.html")
    public String orderListPage(@RequestParam(value = "pageNum", defaultValue = "1") String pageNum, Model model) {
        Map<String, Object> param = new HashMap<>();
        param.put("page", pageNum);

        R r = orderFeignService.listWithItem(param);
        model.addAttribute("orders", r);
        return "orderList";
    }

}
