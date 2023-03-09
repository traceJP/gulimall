package com.tracejp.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import java.util.Map;
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

}