package com.tracejp.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tracejp.gulimall.product.dao.CategoryBrandRelationDao;
import com.tracejp.gulimall.product.entity.CategoryBrandRelationEntity;
import com.tracejp.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang3.RandomUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    private CategoryBrandRelationDao relationDao;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


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

    // * Caching 可以组合操作 Spring-Cache 注解
/*    @Caching(evict = {
            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
            @CacheEvict(value = "category", key = "'getCatelogJson'")
    })*/
    // 或者指定 allEntries 直接删除 当前 cacheName 的所有缓存分区
    @CacheEvict(value = "category", allEntries = true)
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


    /*
        Spring-Cache 总结
        常规数据（读多写少，即时性，一致性要求不高的数据）﹔完全可以使用spring-Cache
        特殊数据:特殊设计
     */


    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        LambdaQueryWrapper<CategoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryEntity::getParentCid, 0);
        return this.list(wrapper);
    }

    // @CachePut 对应 双写模式
    // sync 可以给缓存加锁 防止缓存击穿
    @Cacheable(value = "category", key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {

        List<CategoryEntity> categoryEntities = this.list();

        // 找出 1级 分类
        List<CategoryEntity> level1Categorys = this.getLevel1Categorys();
        return level1Categorys.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
                    // 查找 2级 分类
                    List<CategoryEntity> catelog1 = this.getParentEntity(categoryEntities, l1.getCatId());
                    List<Catelog2Vo> catelog2Vos = null;
                    if (!CollectionUtils.isEmpty(catelog1)) {
                        catelog2Vos = catelog1.stream().map(l2 -> {
                            // 查找 3级 分类
                            List<CategoryEntity> catelog3 = this.getParentEntity(categoryEntities, l2.getCatId());
                            List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                            if (!CollectionUtils.isEmpty(catelog3)) {
                                catelog3Vos = catelog3.stream().map(l3 ->
                                        new Catelog2Vo.Catelog3Vo(
                                                l2.getCatId().toString(),
                                                l3.getCatId().toString(),
                                                l3.getName()
                                        )
                                ).collect(Collectors.toList());
                            }
                            return new Catelog2Vo(
                                    l1.getCatId().toString(),
                                    catelog3Vos,
                                    l2.getCatId().toString(),
                                    l2.getName()
                            );
                        }).collect(Collectors.toList());
                    }
                    return catelog2Vos;
                }));
    }


    /**
     * 使用 redis 缓存解决方案
     * 缓存数据一致性：（缺点：会读脏数据）
     * 1）双写模式：修改了数据库的值时 同时修改缓存中的值
     * 2）失效模式：修改了数据库的值时 直接把缓存中的值删掉
     */
  /*  @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        // 先从缓存中查找
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        if (!StringUtils.isEmpty(catelogJson)) {
            return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        }

        // 缓存中没有，查数据库 & 放入缓存
        return this.getCatelogJsonFormDb();
    }

    // 加锁防止缓存击穿
    private synchronized Map<String, List<Catelog2Vo>> getCatelogJsonFormDb() {

        List<CategoryEntity> categoryEntities = this.list();

        // 找出 1级 分类
        List<CategoryEntity> level1Categorys = this.getLevel1Categorys();
        Map<String, List<Catelog2Vo>> collect = level1Categorys.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
                    // 查找 2级 分类
                    List<CategoryEntity> catelog1 = this.getParentEntity(categoryEntities, l1.getCatId());
                    List<Catelog2Vo> catelog2Vos = null;
                    if (!CollectionUtils.isEmpty(catelog1)) {
                        catelog2Vos = catelog1.stream().map(l2 -> {
                            // 查找 3级 分类
                            List<CategoryEntity> catelog3 = this.getParentEntity(categoryEntities, l2.getCatId());
                            List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                            if (!CollectionUtils.isEmpty(catelog3)) {
                                catelog3Vos = catelog3.stream().map(l3 ->
                                        new Catelog2Vo.Catelog3Vo(
                                                l2.getCatId().toString(),
                                                l3.getCatId().toString(),
                                                l3.getName()
                                        )
                                ).collect(Collectors.toList());
                            }
                            return new Catelog2Vo(
                                    l1.getCatId().toString(),
                                    catelog3Vos,
                                    l2.getCatId().toString(),
                                    l2.getName()
                            );
                        }).collect(Collectors.toList());
                    }
                    return catelog2Vos;
                }));

        // 缓存到 redis
        String s = JSON.toJSONString(collect);
        // 添加过期时间随机 - 防止缓存雪崩
        redisTemplate.opsForValue().set("catelogJson", s, 2L * RandomUtils.nextInt(12, 24), TimeUnit.HOURS);

        return collect;
    }
*/

    private List<CategoryEntity> getParentEntity(List<CategoryEntity> categoryEntities, Long parentCid) {
        return categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(parentCid))
                .collect(Collectors.toList());
    }


}