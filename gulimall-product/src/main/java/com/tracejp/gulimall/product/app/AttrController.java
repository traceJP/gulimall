package com.tracejp.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.tracejp.gulimall.product.entity.ProductAttrValueEntity;
import com.tracejp.gulimall.product.service.ProductAttrValueService;
import com.tracejp.gulimall.product.vo.AttrResponseVo;
import com.tracejp.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tracejp.gulimall.product.service.AttrService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.R;



/**
 * 商品属性
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 19:30:23
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;


    /**
     * 规格
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseListForSpu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> data = productAttrValueService.baseListForSpu(spuId);
        return R.ok().put("data", data);
    }

    /**
     * /product/attr/sale/list/{catelogId}
     * /product/attr/base/list/{catelogId}
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String attrType){

        PageUtils page = attrService.baseAttrQuery(params, catelogId, attrType);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     * @return AttrResponseVo
     */
    @RequestMapping("/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrResponseVo attrResponseVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrResponseVo);
    }

    /**
     * 保存 - 级联保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R saveAttr(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改 - 级联修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 规格修改
     */
    @PostMapping("/update/{spuId}")
    public R update(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId, entities);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
