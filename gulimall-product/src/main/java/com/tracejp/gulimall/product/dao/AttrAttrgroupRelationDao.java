package com.tracejp.gulimall.product.dao;

import com.tracejp.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tracejp.gulimall.product.vo.AttrGroupRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 19:11:22
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    /**
     * 批量删除
     * DELETE FROM `pms_attr_attrgroup_relation` WHERE (attr_id = ? AND attr_group_id = ?) OR ...
     */
    void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);
}
