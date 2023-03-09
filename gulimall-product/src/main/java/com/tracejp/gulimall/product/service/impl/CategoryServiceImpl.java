package com.tracejp.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tracejp.gulimall.product.dao.CategoryBrandRelationDao;
import com.tracejp.gulimall.product.entity.CategoryBrandRelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.product.dao.CategoryDao;
import com.tracejp.gulimall.product.entity.CategoryEntity;
import com.tracejp.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    private CategoryBrandRelationDao relationDao;

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

/*
    // findCatelogPath 递归写法 1
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new LinkedList<>();
        List<Long> parentPath = this.findParentPath(catelogId, paths);
        Collections.reverse(parentPath);    // 逆序 parentPath
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    // 递归查 catelogId 路径上的所有节点 catelogId 值
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        // SELECT * FROM pms_category WHERE cat_id = catelogId
        Long parentCid = this.getById(catelogId).getParentCid();

        // 继续找
        if (parentCid != 0) {
            findParentPath(parentCid, paths);
        }

        // == 0
        return paths;
    }
*/

    // findCatelogPath 递归写法 2
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new LinkedList<>();
        // 传递引用 不需要返回值
        this.findParentPath(catelogId, paths);
        Collections.reverse(paths);

        return paths.toArray(new Long[paths.size()]);
    }

    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {

        this.updateById(category);

        // 同步更新其他关联表中的数据
        if (!StringUtils.isEmpty(category.getName())) {
            CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
            categoryBrandRelationEntity.setCatelogName(category.getName());
            LambdaQueryWrapper<CategoryBrandRelationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CategoryBrandRelationEntity::getCatelogId, category.getCatId());
            relationDao.update(categoryBrandRelationEntity, wrapper);
        }

        // TODO 更新其他关联表

    }

    // 递归查 catelogId 路径上的所有节点 catelogId 值
    private void findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        // SELECT * FROM pms_category WHERE cat_id = catelogId
        Long parentCid = this.getById(catelogId).getParentCid();
        if (parentCid != 0) {
            findParentPath(parentCid, paths);
        }
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