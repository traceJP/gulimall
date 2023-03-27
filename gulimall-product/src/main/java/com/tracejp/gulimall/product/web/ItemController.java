package com.tracejp.gulimall.product.web;

import com.tracejp.gulimall.product.service.SkuInfoService;
import com.tracejp.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 14:26
 */
@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;


    @GetMapping({"/{skuId}.html"})
    public String skuItem(@PathVariable Long skuId, Model model) {
        SkuItemVo vo = skuInfoService.item(skuId);

        model.addAttribute("item", vo);
        return "item";
    }



}
