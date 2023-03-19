package com.tracejp.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tracejp.gulimall.product.vo.BrandVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tracejp.gulimall.product.entity.CategoryBrandRelationEntity;
import com.tracejp.gulimall.product.service.CategoryBrandRelationService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 19:30:23
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;


    /**
     * 通过 categoryId 查询 brandId 和 brandName
     * 直接查询 pms_category_brand_relation 关联表即可 有冗余字段
     * /product/categorybrandrelation/brands/list?t=1678585500424&catId=225
     */
    @GetMapping("/brands/list")
    public R relationBrandsList(@RequestParam(value = "catId") Long catId) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryBrandRelationEntity::getCatelogId, catId);
        List<BrandVo> data = categoryBrandRelationService.list(wrapper).stream()
                .map(item -> {
                    BrandVo brandVo = new BrandVo();
                    BeanUtils.copyProperties(item, brandVo);
                    return brandVo;
                })
                .collect(Collectors.toList());
        return R.ok().put("data", data);
    }


    /**
     * 通过brandId 查询 返回关联关系
     * product/categorybrandrelation/catelog/list
     */
    @GetMapping("/catelog/list")
    public R catelogList(@RequestParam("brandId") Long brandId) {

        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(
                new LambdaQueryWrapper<CategoryBrandRelationEntity>()
                        .eq(CategoryBrandRelationEntity::getBrandId, brandId)
        );

    	return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){

        // pms_category_brand_relation 表具有冗余字段，所以需要在保存的时候，将品牌名和分类名保存到表中
        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
