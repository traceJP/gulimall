package com.tracejp.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.ware.dao.WareInfoDao;
import com.tracejp.gulimall.ware.entity.WareInfoEntity;
import com.tracejp.gulimall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询处理
        String queryKey = (String) params.get("key");
        if (!StringUtils.isEmpty(queryKey)) {
            wrapper.eq(WareInfoEntity::getId, queryKey)
                    .or().like(WareInfoEntity::getName, queryKey)
                    .or().like(WareInfoEntity::getAddress, queryKey)
                    .or().like(WareInfoEntity::getAreacode, queryKey);
        }

        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

}