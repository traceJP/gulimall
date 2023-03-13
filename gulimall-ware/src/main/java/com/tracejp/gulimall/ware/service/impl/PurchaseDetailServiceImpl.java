package com.tracejp.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tracejp.gulimall.ware.entity.WareInfoEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.ware.dao.PurchaseDetailDao;
import com.tracejp.gulimall.ware.entity.PurchaseDetailEntity;
import com.tracejp.gulimall.ware.service.PurchaseDetailService;
import org.springframework.util.StringUtils;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = new LambdaQueryWrapper<>();

        String queryKey = (String) params.get("key");
        if (!StringUtils.isEmpty(queryKey)) {
            wrapper.and(w -> w.eq(PurchaseDetailEntity::getSkuId, queryKey).or().like(PurchaseDetailEntity::getId, queryKey));
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq(PurchaseDetailEntity::getPurchaseId, status);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq(PurchaseDetailEntity::getId, wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseDetailEntity::getPurchaseId, id);
        return this.list(wrapper);
    }

}