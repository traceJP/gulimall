package com.tracejp.gulimall.seckill.controller;

import com.tracejp.common.to.SeckillSkuRedisTo;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/11 8:47
 */
@Controller
public class SeckillController {

    @Autowired
    private SeckillService seckillService;


    @ResponseBody
    @GetMapping("/getCurrentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> tos = seckillService.getCurrentSeckillSkus();
        return R.ok().put("data", tos);
    }

    /**
     * 获取当前sku的秒杀信息（该接口用于判断当前sku是否有秒杀活动，无关秒杀场次）
     */
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().put("data", to);
    }

    /**
     * @param killId sessionId_skuId
     * @param key    秒杀随机码 code
     * @param num    秒杀数量
     */
    @GetMapping("/kill")
    public String kill(
            @RequestParam("killId") String killId,
            @RequestParam("key") String key,
            @RequestParam("num") Integer num,
            Model model) {
        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }

}
