package com.tracejp.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.product.dao.CategoryDao;
import com.tracejp.gulimall.product.entity.CategoryEntity;
import com.tracejp.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);

    }


    @Override
    public List<CategoryEntity> listWithTree() {

        // 1. 查出所有分类    一级分类默认为 0
        List<CategoryEntity> entities = super.list();

        return entities.stream()
                // 找出所有的一级分类
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .peek(menu -> menu.setChildren(this.getChilerens(menu, entities)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChilerens(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                // 过滤出子菜单 - 可以过滤出当前 根 的 所有 父级 菜单
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                // 递归 - 最终到达叶子节点 即找到了非叶子节点的子菜单 并且添加到当前节点的 children 中
                .peek(categoryEntity -> categoryEntity.setChildren(getChilerens(categoryEntity, all)))
                // 排序 - comparingInt 传入 ToIntFunction 参数 即 双参数 实现 int 对比
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

}