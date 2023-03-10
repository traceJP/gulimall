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

        // ?????? pms_attr
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        // ???????????? pms_attr_group_relation - ????????????
        if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode().equals(attr.getAttrType()) // ??????????????????????????????
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

        // ?????? attrType ????????????
        switch (attrType) {
            case "base":
                wrapper.eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
                break;
            case "sale":
                wrapper.eq(AttrEntity::getAttrType,  ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
                break;
        }

        // ????????????????????????
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

        // ?????? AttrResponseVo
        List<AttrResponseVo> responseVoList = page.getRecords().stream().map(attrEntity -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);

            // ???????????????
            // ??? attr_group_relation ???????????? attr_group_id ??? ???????????????????????????id ?????? selectOne
            // ?????? attr_group ???????????? attr_group_name
            if ("base".equalsIgnoreCase(attrType)) { // ??????????????????????????????
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao.selectOne(
                        new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId())
                );
                // ?????????????????????????????????????????????
                if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            // ???????????????
            // ??? category ???????????? category_name
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

        // ????????????id
        // ?????? attrId ?????? attr_group_id
        if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode().equals(attrEntity.getAttrType())) { // ??????????????????????????????
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao.selectOne(
                    new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, attrId)
            );
            if (attrAttrgroupRelationEntity != null) {
                attrResponseVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());

                // ?????? attr_group_name
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }

            }
        }

        // ???????????????????????????
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

        // ????????????
        if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode().equals(attrEntity.getAttrType())) { // ??????????????????????????????
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId());
            Integer count = relationDao.selectCount(queryWrapper);
            // build entity
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            LambdaUpdateWrapper<AttrAttrgroupRelationEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId());
            if (count > 0) { // ??????
                relationDao.update(attrAttrgroupRelationEntity, wrapper);
            } else { // ??????
                relationDao.insert(attrAttrgroupRelationEntity);
            }
        }

    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {

        // ??? pms_attr_attrgroup_relation????????? ????????? attrId ??????
        // ?????? attrId ?????? ?????? attrEntity ??????
        List<AttrAttrgroupRelationEntity> relationEntityList = relationDao.selectList(
                new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupId)
        );

        // ?????? attrId ??????
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

        // TODO ?????????????????????????????? ??????P81??????????????????
        // - ?????????????????????????????????????????????????????????
        // - ?????????????????????????????????????????????????????????

        // ???????????????
        // pms_attr_group ????????? attr_group_id?????? ???attr_group_id???
        // ??? pms_attr_attrgroup_relation ????????? attrId?????? ???attr_id???
        // ??? pms_attr ????????? attrEntity?????? && ?????????

        // 1 ?????? attr_group_id ?????? catelog_id  ?????????????????? ???????????? ??? attr_group_id??????
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        List<Long> groupIds = attrGroupDao.selectList(
                new LambdaQueryWrapper<AttrGroupEntity>()
                        .eq(AttrGroupEntity::getCatelogId, attrGroupEntity.getCatelogId())
        ).stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());

        // 2 ????????? pms_attr_attrgroup_relation????????? ????????????????????? attrId??????
        List<Long> attrIds = relationDao.selectList(
                new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                        .in(AttrAttrgroupRelationEntity::getAttrGroupId, groupIds)
        ).stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        // 3 ?????? attrEntity??????
        // ???????????????????????? attrIds ???????????? attrId????????????????????????
        LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<AttrEntity>()
                .eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) // ??????????????????????????????
                .eq(AttrEntity::getCatelogId, attrGroupEntity.getCatelogId());
        if (!CollectionUtils.isEmpty(attrIds)) {
            wrapper.notIn(AttrEntity::getAttrId, attrIds);
        }

        // ???????????????????????????
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

        // ????????? AttrAttrgroupRelationEntity
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = Arrays.stream(attrGroupRelationVos)
                .map(attrGroupRelationVo -> {
                    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(attrGroupRelationVo, attrAttrgroupRelationEntity);
                    return attrAttrgroupRelationEntity;
                }).collect(Collectors.toList());

        // ????????????
        relationDao.deleteBatchRelation(attrAttrgroupRelationEntities);
    }



}