package com.tracejp.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.tracejp.gulimall.product.entity.AttrEntity;
import com.tracejp.gulimall.product.service.AttrAttrgroupRelationService;
import com.tracejp.gulimall.product.service.AttrService;
import com.tracejp.gulimall.product.service.CategoryService;
import com.tracejp.gulimall.product.vo.AttrGroupRelationVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tracejp.gulimall.product.entity.AttrGroupEntity;
import com.tracejp.gulimall.product.service.AttrGroupService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.R;



/**
 * 属性分组
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 19:30:23
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;


    /**
     * 新增关联关系
     * /attrgroup/attr/relation
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody AttrGroupRelationVo[] attrGroupRelationVo) {
        relationService.saveBatch(Arrays.asList(attrGroupRelationVo));

        return R.ok();
    }


    /**
     * 获取分类下所有分组&关联属性
     * /1/attr/relation
     */
    @GetMapping("/{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId) {
        List<AttrEntity> attrEntities = attrService.getRelationAttr(attrGroupId);

        return R.ok().put("data", attrEntities);
    }

    /**
     *  获取分类下所有分组& 没有关联的属性 - 分页方法
     * /1/noattr/relation
     */
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params, @PathVariable("attrGroupId") Long attrGroupId) {
        PageUtils page = attrService.getNoRelationAttr(params, attrGroupId);

        return R.ok().put("page", page);
    }



    /**
     * 关联删除 - 批量删除
     * /attr/relation/delete
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] attrGroupRelationVos) {
        attrService.deleteRelation(attrGroupRelationVos);

        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId){
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){

		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

		// TODO 这里如果抛出了异常，返回到前端会显示 404 错误

		// 查出 catelogPath
        Long[] path = categoryService.findCatelogPath(attrGroup.getCatelogId());
        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
