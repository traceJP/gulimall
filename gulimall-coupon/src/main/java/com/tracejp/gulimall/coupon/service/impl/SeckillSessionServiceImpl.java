package com.tracejp.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;
import com.tracejp.gulimall.coupon.dao.SeckillSessionDao;
import com.tracejp.gulimall.coupon.entity.SeckillSessionEntity;
import com.tracejp.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.tracejp.gulimall.coupon.service.SeckillSessionService;
import com.tracejp.gulimall.coupon.service.SeckillSkuRelationService;
import com.tracejp.gulimall.coupon.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        LambdaQueryWrapper<SeckillSessionEntity> wrapper = new LambdaQueryWrapper<>();
        String startTime = TimeUtil.getNowAsStart()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTime = TimeUtil.getNowAsEnd().plusDays(2)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        wrapper.between(SeckillSessionEntity::getStartTime, startTime, endTime);
        List<SeckillSessionEntity> list = this.list(wrapper);
        // 查出关联的商品
        for (SeckillSessionEntity seckillSessionEntity : list) {
            List<SeckillSkuRelationEntity> relations =
                    seckillSkuRelationService.getBySessionId(seckillSessionEntity.getId());
            seckillSessionEntity.setRelationSkus(relations);
        }
        return list;
    }

}