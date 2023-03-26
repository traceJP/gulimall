package com.tracejp.gulimall.product.web;

import com.tracejp.gulimall.product.entity.CategoryEntity;
import com.tracejp.gulimall.product.service.CategoryService;
import com.tracejp.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/19 11:17
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categories", categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {

        return categoryService.getCatelogJson();
    }


}
