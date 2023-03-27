package com.tracejp.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tracejp.gulimall.product.entity.AttrEntity;
import com.tracejp.gulimall.product.service.AttrService;
import com.tracejp.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.tracejp.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.product.dao.AttrGroupDao;
import com.tracejp.gulimall.product.entity.AttrGroupEntity;
import com.tracejp.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        // 注意： MP page方法需要配置分页插件 否则不起作用
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        // SELECT * FROM pms_att_group WHERE catelog_id = ? AND (att_group_id = key OR attr_group_name like %?%)
        LambdaQueryWrapper<AttrGroupEntity> wrapper = new LambdaQueryWrapper<>();

        // 拿到查询检索条件
        String queryKey = (String) params.get("key");
        if (!StringUtils.isEmpty(queryKey)) {
            wrapper.and(obj -> obj.eq(AttrGroupEntity::getAttrGroupId, queryKey)
                    .or()
                    .like(AttrGroupEntity::getAttrGroupName, queryKey));
        }

        // 业务要求：默认没有选择三级分类（前端传入 catelogId = 0），则是查询所有
        if (catelogId != 0) {
            wrapper.eq(AttrGroupEntity::getCatelogId, catelogId);
        }

        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrByCatelogId(Long catelogId) {

        // 1 在 pms_attr_group表中查询出所有的分组id
        // 2 再在 pms_attr_attrgroup_relation 关联表中查询出所有 attr_id
        // 3 再把 attrEntity 查出
        // (2, 3步骤) 在 attrService中已经有方法，直接调用即可

//        LambdaQueryWrapper<AttrGroupEntity> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(AttrGroupEntity::getCatelogId, catelogId);
//        List<AttrGroupWithAttrsVo> vos = this.list(wrapper).stream()
//                .map(attrGroupEntity -> {
//                    AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
//                    BeanUtils.copyProperties(attrGroupEntity, vo);
//                    return vo;
//                })
//                .peek(attrGroupWithAttrsVo -> {
//                    List<Long> attrIds = relationDao.selectList(
//                            new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
//                                    .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupWithAttrsVo.getAttrGroupId())
//                    ).stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
//                    attrGroupWithAttrsVo.setAttrs(attrDao.selectBatchIds(attrIds));
//                })
//                .collect(Collectors.toList());
        LambdaQueryWrapper<AttrGroupEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttrGroupEntity::getCatelogId, catelogId);
        List<AttrGroupEntity> groups = this.list(wrapper);
        List<AttrGroupWithAttrsVo> vos = groups.stream().map(group -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, vo);
            List<AttrEntity> attrEntities = attrService.getRelationAttr(vo.getAttrGroupId());
            vo.setAttrs(attrEntities);
            return vo;
        }).collect(Collectors.toList());

        return vos;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {

        return this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }

}