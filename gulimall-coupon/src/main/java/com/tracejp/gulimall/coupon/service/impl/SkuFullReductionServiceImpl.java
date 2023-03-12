package com.tracejp.gulimall.coupon.service.impl;

import com.tracejp.common.to.MemberPrice;
import com.tracejp.common.to.SkuReductionTo;
import com.tracejp.gulimall.coupon.entity.MemberPriceEntity;
import com.tracejp.gulimall.coupon.entity.SkuLadderEntity;
import com.tracejp.gulimall.coupon.service.MemberPriceService;
import com.tracejp.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.coupon.dao.SkuFullReductionDao;
import com.tracejp.gulimall.coupon.entity.SkuFullReductionEntity;
import com.tracejp.gulimall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {

        // 保存优惠信息 sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReductionTo, skuLadderEntity);
        if (skuLadderEntity.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        // 保存满减信息 sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
            this.save(skuFullReductionEntity);
        }

        // 保存会员价格 sms_member_price
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        if (!CollectionUtils.isEmpty(memberPrice)) {
            List<MemberPriceEntity> memberPriceEntities = memberPrice.stream().map(item -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                memberPriceEntity.setMemberLevelId(item.getId());
                memberPriceEntity.setMemberLevelName(item.getName());
                memberPriceEntity.setMemberPrice(item.getPrice());
                memberPriceEntity.setAddOther(1);
                return memberPriceEntity;
            }).filter(item -> item.getMemberPrice().compareTo(new BigDecimal("0")) == 1)
                    .collect(Collectors.toList());
            memberPriceService.saveBatch(memberPriceEntities);
        }

    }

}