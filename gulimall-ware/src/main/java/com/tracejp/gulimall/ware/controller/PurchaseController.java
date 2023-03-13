package com.tracejp.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.tracejp.gulimall.ware.vo.MergeVo;
import com.tracejp.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tracejp.gulimall.ware.entity.PurchaseEntity;
import com.tracejp.gulimall.ware.service.PurchaseService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.R;



/**
 * 采购信息
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:16:23
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    @PostMapping("/done")
    public R finishPurchase(@RequestBody PurchaseDoneVo purchaseDoneVo) {
        purchaseService.finishPurchase(purchaseDoneVo);
        return R.ok();
    }

    @PostMapping("/received")
    public R receivedPurchase(@RequestBody List<Long> ids) {
        purchaseService.receivedPurchase(ids);
        return R.ok();
    }

    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo) {
        purchaseService.mergePurchase(mergeVo);
        return R.ok();
    }

    @GetMapping("/unreceive/list")
    public R unreceivedList(@RequestParam Map<String, Object> params) {
    	PageUtils page = purchaseService.queryPageUnreceivedList(params);

    	return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
