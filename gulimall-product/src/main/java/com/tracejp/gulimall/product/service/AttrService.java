package com.tracejp.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.product.entity.AttrEntity;
import com.tracejp.gulimall.product.vo.AttrGroupRelationVo;
import com.tracejp.gulimall.product.vo.AttrResponseVo;
import com.tracejp.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 19:11:22
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils baseAttrQuery(Map<String, Object> params, Long catelogId, String attrType);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrGroupId);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId);

    void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVos);

    /**
     * 过滤可被检索的 attr 属性
     */
    List<Long> selectSearchAttrs(List<Long> attrIds);
}

