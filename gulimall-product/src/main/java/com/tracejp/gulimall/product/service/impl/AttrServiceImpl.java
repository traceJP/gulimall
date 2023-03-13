package com.tracejp.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tracejp.common.constant.ProductConstant;
import com.tracejp.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.tracejp.gulimall.product.dao.AttrGroupDao;
import com.tracejp.gulimall.product.dao.CategoryDao;
import com.tracejp.gulimall.product.entity.*;
import com.tracejp.gulimall.product.service.CategoryService;
import com.tracejp.gulimall.product.vo.AttrGroupRelationVo;
import com.tracejp.gulimall.product.vo.AttrResponseVo;
import com.tracejp.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.product.dao.AttrDao;
import com.tracejp.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveAttr(AttrVo attr) {

        // 保存 pms_attr
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        // 级联保存 pms_attr_group_relation - 关联关系
        if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode().equals(attr.getAttrType()) // 只有基本属性才有分组
                && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationDao.insert(relationEntity);
        }

    }

    @Override
    public PageUtils baseAttrQuery(Map<String, Object> params, Long catelogId, String attrType) {

        LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<>();

        // 根据 attrType 进行查询
        switch (attrType) {
            case "base":
                wrapper.eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
                break;
            case "sale":
                wrapper.eq(AttrEntity::getAttrType,  ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
                break;
        }

        // 拿到查询检索条件
        String queryKey = (String) params.get("key");
        if (!StringUtils.isEmpty(queryKey)) {
            wrapper.and(obj -> obj.eq(AttrEntity::getAttrId, queryKey)
                    .or()
                    .like(AttrEntity::getAttrName, queryKey));
        }
        if (catelogId != 0) {
            wrapper.eq(AttrEntity::getCatelogId, catelogId);
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        // 映射 AttrResponseVo
        List<AttrResponseVo> responseVoList = page.getRecords().stream().map(attrEntity -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);

            // 找到分组名
            // 在 attr_group_relation 表中找到 attr_group_id ； 关联表中有多条相同id 所以 selectOne
            // 再在 attr_group 表中找到 attr_group_name
            if ("base".equalsIgnoreCase(attrType)) { // 只有基本属性才有分组
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao.selectOne(
                        new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId())
                );
                // 判空：首次可能没有添加关联关系
                if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            // 找到分类名
            // 在 category 表中找到 category_name
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }

            return attrResponseVo;
        }).collect(Collectors.toList());

        // Build PageUtils
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(responseVoList);
        return pageUtils;

    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrResponseVo);

        // 所属分组id
        // 通过 attrId 找到 attr_group_id
        if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode().equals(attrEntity.getAttrType())) { // 只有基本属性才有分组
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao.selectOne(
                    new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, attrId)
            );
            if (attrAttrgroupRelationEntity != null) {
                attrResponseVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());

                // 拿到 attr_group_name
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }

            }
        }

        // 所属分类的完整路径
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrResponseVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
        if (categoryEntity != null) {
            attrResponseVo.setCatelogName(categoryEntity.getName());
        }

        return attrResponseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        // vo -> entity
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);

        this.updateById(attrEntity);

        // 修改关联
        if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode().equals(attrEntity.getAttrType())) { // 只有基本属性才有分组
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId());
            Integer count = relationDao.selectCount(queryWrapper);
            // build entity
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            LambdaUpdateWrapper<AttrAttrgroupRelationEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId());
            if (count > 0) { // 修改
                relationDao.update(attrAttrgroupRelationEntity, wrapper);
            } else { // 新增
                relationDao.insert(attrAttrgroupRelationEntity);
            }
        }

    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {

        // 从 pms_attr_attrgroup_relation关联表 中找到 attrId 集合
        // 通过 attrId 集合 返回 attrEntity 集合
        List<AttrAttrgroupRelationEntity> relationEntityList = relationDao.selectList(
                new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupId)
        );

        // 映射 attrId 集合
        if (!CollectionUtils.isEmpty(relationEntityList)) {
            List<Long> attrIds = relationEntityList.stream()
                    .map(AttrAttrgroupRelationEntity::getAttrId)
                    .collect(Collectors.toList());

            Collection<AttrEntity> attrEntities = this.listByIds(attrIds);

            return new ArrayList<>(attrEntities);
        }

        return null;

    }


    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId) {

        // TODO 创建关联的业务要求： 视频P81（没听明白）
        // - 当前分类只能关联自己所属分类里面的属性
        // - 当前分组只能关联别的分组没有引用的属性

        // 查询流程：
        // pms_attr_group 中拿到 attr_group_id集合 （attr_group_id）
        // 》 pms_attr_attrgroup_relation 中拿到 attrId集合 （attr_id）
        // 》 pms_attr 中拿到 attrEntity集合 && 排除掉

        // 1 通过 attr_group_id 找到 catelog_id  然后拿到所有 已经关联 的 attr_group_id集合
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        List<Long> groupIds = attrGroupDao.selectList(
                new LambdaQueryWrapper<AttrGroupEntity>()
                        .eq(AttrGroupEntity::getCatelogId, attrGroupEntity.getCatelogId())
        ).stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());

        // 2 再通过 pms_attr_attrgroup_relation关联表 拿到对应的全部 attrId集合
        List<Long> attrIds = relationDao.selectList(
                new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                        .in(AttrAttrgroupRelationEntity::getAttrGroupId, groupIds)
        ).stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        // 3 拿到 attrEntity集合
        // 拿取集合中排除掉 attrIds 集合中的 attrId，即不能包含自己
        LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<AttrEntity>()
                .eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) // 只有基本属性才能关联
                .eq(AttrEntity::getCatelogId, attrGroupEntity.getCatelogId());
        if (!CollectionUtils.isEmpty(attrIds)) {
            wrapper.notIn(AttrEntity::getAttrId, attrIds);
        }

        // 构造分页查询并返回
        String queryKey = (String) params.get("key");
        if (!StringUtils.isEmpty(queryKey)) {
            wrapper.and(obj -> obj.eq(AttrEntity::getAttrId, queryKey)
                    .or()
                    .like(AttrEntity::getAttrName, queryKey));
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        return new PageUtils(page);

    }


    @Override
    public void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVos) {

        // 映射到 AttrAttrgroupRelationEntity
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = Arrays.stream(attrGroupRelationVos)
                .map(attrGroupRelationVo -> {
                    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(attrGroupRelationVo, attrAttrgroupRelationEntity);
                    return attrAttrgroupRelationEntity;
                }).collect(Collectors.toList());

        // 批量删除
        relationDao.deleteBatchRelation(attrAttrgroupRelationEntities);
    }



}